package finity.fini.service.Popularity;

import finity.fini.apiPayload.code.status.ErrorStatus;
import finity.fini.apiPayload.exception.handler.PopularityHandler;
import finity.fini.domain.ProductBase;
import finity.fini.domain.ProductPopularity;
import finity.fini.dto.Popularity.NaverDataLabRequestDTO;
import finity.fini.dto.Popularity.NaverDataLabResponseDTO;
import finity.fini.dto.Popularity.NaverSearchDTO;
import finity.fini.dto.Popularity.RankingTaskDTO;
import finity.fini.repository.DepositProductRepository;
import finity.fini.repository.ProductPopularityRepository;
import finity.fini.repository.SavingProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchRankingService {
    private final SavingProductRepository savingProductRepository;
    private final DepositProductRepository depositProductRepository;
    private final ProductPopularityRepository popularityRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${naver.api.client-id}")
    private String naverClientId;
    @Value("${naver.api.client-secret}")
    private String naverClientSecret;
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Scheduled(cron = "0 0 7 1 * *")
    public void updatePopularityRank() {
        log.info("### (DTO + Parallel) 배치 작업을 시작합니다. ###");

        try {
            // 1. 적금 상품 처리
            processProductPages(true);

            // 2. 예금 상품 처리
            processProductPages(false);

            log.info("### 배치 작업을 완료했습니다. ###");

        } catch (Exception e) {
            log.error("배치 작업 중 치명적 오류 발생", e);
            // 수동 실행 시 컨트롤러로 에러를 전파하기 위해 Custom Exception 발생
            throw new PopularityHandler(ErrorStatus.RANKING_UPDATE_FAILED);
        }
    }

    private void processProductPages(boolean isSaving) {
        int pageNumber = 0;
        while (true) {
            PageRequest pageRequest = PageRequest.of(pageNumber, 20);
            Page<? extends ProductBase> page;

            try {
                if (isSaving) {
                    page = savingProductRepository.findAll(pageRequest);
                } else {
                    page = depositProductRepository.findAll(pageRequest);
                }
            } catch (Exception e) {
                log.error("DB 조회 중 오류 발생 (Page: {})", pageNumber, e);
                throw new PopularityHandler(ErrorStatus.RANKING_UPDATE_FAILED);
            }

            log.info("{} 상품 처리 중... Page {}/{}", isSaving ? "적금" : "예금", page.getNumber() + 1, page.getTotalPages());

            List<RankingTaskDTO> tasks = page.getContent().stream()
                    .map(product -> RankingTaskDTO.builder()
                            .productId(product.getProductId())
                            .productType(product.getProductTypeEnum())
                            .bankName(product.getBank().getKorCoNm()) // Lazy Loading 발생 (여기선 안전)
                            .productName(product.getFinPrdtNm())
                            .specialCondition(product.getSpclCnd())
                            .build())
                    .toList();

            for (RankingTaskDTO task : tasks) {
                processTask(task);

                // [중요 수정] API 호출 간 딜레이 추가 (Gemini/Naver 제한 방지)
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (!page.hasNext()) break;
            pageNumber++;
        }
    }

    private void processTask(RankingTaskDTO task) {
        String keyword = task.getBankName() + " " + task.getProductName();
        try {
            // 1. Naver 뉴스 API
            NaverSearchDTO newsResult = callNaverNewsApi(keyword);
            int newsCount = (newsResult.getTotal() != null) ? newsResult.getTotal() : 0;
            List<String> newsSnippets = newsResult.getItems().stream()
                    .map(NaverSearchDTO.Item::getDescription)
                    .limit(3)
                    .toList();

            // 2. Naver DataLab API
            double searchScore = callNaverDataLabApi(keyword);

            // 3. 최종 점수 계산
            double finalScore = (searchScore * 1.0) + (newsCount * 0.5);

            // 4. Gemini API (RAG)
            String aiSummary = null;
            try {
                String productInfo = "우대조건: " + task.getSpecialCondition();
                String prompt = buildPrompt(productInfo, newsSnippets);
                aiSummary = callGeminiApiWithRetry(prompt);
            } catch (PopularityHandler e) {
                // [수정 2] 실패 시 기본 멘트로 덮어쓰지 않고, 로그만 남김 (aiSummary는 null 상태 유지)
                log.warn("Gemini 요약 실패 (상품: {}) - 기존 요약을 유지하거나 생성을 건너뜁니다.", task.getProductName());
            }

            // 5. DB 저장
            savePopularity(task, finalScore, aiSummary);


        } catch (Exception e) {
            log.error("상품 랭킹 업데이트 실패: {} - {}", keyword, e.getMessage());
        }
    }

    @Transactional
    protected void savePopularity(RankingTaskDTO task, double score, String aiSummary) {
        // 1. 기존 데이터 조회
        Optional<ProductPopularity> existingPop = popularityRepository.findByProductIdAndProductType(task.getProductId(), task.getProductType());

        if (existingPop.isPresent()) {

            existingPop.get().updateData(score, aiSummary);

            popularityRepository.save(existingPop.get());
        } else {
            // 3. 없으면 -> 새로 생성 (ID는 자동 생성이므로 설정 필요 없음)
            ProductPopularity newPop = ProductPopularity.builder()
                    .productId(task.getProductId())
                    .productType(task.getProductType())
                    .popularityScore(score)
                    .aiSummary(aiSummary)
                    .build();
            popularityRepository.save(newPop);
        }
    }


    private NaverSearchDTO callNaverNewsApi(String keyword) {
        try {
            WebClient webClient = webClientBuilder.baseUrl("https://openapi.naver.com").build();
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/search/news.json")
                            .queryParam("query", keyword)
                            .queryParam("display", 5)
                            .build())
                    .header("X-Naver-Client-Id", naverClientId)
                    .header("X-Naver-Client-Secret", naverClientSecret)
                    .retrieve()
                    .bodyToMono(NaverSearchDTO.class)
                    .block();
        } catch (Exception e) {
            log.warn("Naver 뉴스 API 호출 실패: {}", keyword, e);
            // 필수 데이터이므로 예외를 던져서 Ranking 로직에서 제외시킴
            throw new PopularityHandler(ErrorStatus.NAVER_API_ERROR);
        }
    }

    private double callNaverDataLabApi(String keyword) {
        try {
            WebClient webClient = webClientBuilder.baseUrl("https://openapi.naver.com").build();
            String endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String startDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE);

            NaverDataLabRequestDTO requestBody = NaverDataLabRequestDTO.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .timeUnit("date")
                    .keywordGroups(List.of(
                            NaverDataLabRequestDTO.KeywordGroup.builder()
                                    .groupName(keyword)
                                    .keywords(List.of(keyword))
                                    .build()
                    ))
                    .build();

            NaverDataLabResponseDTO response = webClient.post()
                    .uri("/datalab/search")
                    .header("X-Naver-Client-Id", naverClientId)
                    .header("X-Naver-Client-Secret", naverClientSecret)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(NaverDataLabResponseDTO.class)
                    .block();

            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                return response.getResults().get(0).getData().stream()
                        .mapToDouble(NaverDataLabResponseDTO.Data::getRatio)
                        .sum();
            }
            return 0.0; // 검색 결과 없으면 0점 (정상 케이스)

        } catch (WebClientResponseException e) {
            log.warn("Naver 데이터랩 API 호출 실패: {}", keyword, e);
            throw new PopularityHandler(ErrorStatus.NAVER_API_ERROR);
        } catch (Exception e) {
            log.warn("Naver 데이터랩 알 수 없는 오류", e);
            throw new PopularityHandler(ErrorStatus.NAVER_API_ERROR);
        }
    }

    private String callGeminiApiWithRetry(String prompt) {
        int maxRetries = 3;
        int retryDelay = 5000;

        for (int i = 0; i < maxRetries; i++) {
            try {
                return callGeminiApiInternal(prompt);
            } catch (WebClientResponseException e) {
                if (e.getStatusCode().value() == 429 || e.getStatusCode().value() == 503) {
                    log.warn("Gemini API 과부하 ({}). 재시도 {}/{}", e.getStatusCode(), i + 1, maxRetries);
                    try {
                        Thread.sleep(retryDelay);
                        retryDelay += 3000;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new PopularityHandler(ErrorStatus.GEMINI_API_ERROR);
                    }
                } else {
                    // 400 등 다른 에러는 재시도 없이 즉시 실패
                    throw new PopularityHandler(ErrorStatus.GEMINI_API_ERROR);
                }
            } catch (Exception e) {
                log.error("Gemini 호출 중 알 수 없는 오류", e);
                throw new PopularityHandler(ErrorStatus.GEMINI_API_ERROR);
            }
        }
        // 재시도 횟수 초과
        throw new PopularityHandler(ErrorStatus.GEMINI_API_ERROR);
    }

    private String callGeminiApiInternal(String prompt) {
        WebClient webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();
        String url = "/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        Map<String, Object> response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String text = (String) parts.get(0).get("text");
            return text != null ? text.trim() : "정보 없음";
        } catch (Exception e) {
            throw new RuntimeException("Parsing Error");
        }
    }


    private String buildPrompt(String productInfo, List<String> newsSnippets) {
        String newsText = newsSnippets.stream()
                .map(s -> "- " + s.replaceAll("<[^>]*>", ""))
                .collect(Collectors.joining(" "));

        return String.format(
            """
            금융 상품의 우대조건과 관련 뉴스를 보고, 이 상품의 핵심 혜택을 **25자 이내로 짧게** 소개해줘.
            [우대조건]: %s
            [뉴스 키워드]: %s
            조건: 상품명과 금리 수치는 절대 포함하지 마, 25자 이내로 작성하고 우대조건의 핵심만 언급해. '~시 유리합니다' 또는 '~에 적합합니다' 형태로 작성해. "**" 같은 마크다운 언어는 절대 쓰지마.
            요약:""", productInfo, newsText.isBlank() ? "없음" : newsText
        );
    }

}