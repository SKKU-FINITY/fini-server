package finity.fini.service.Popularity;

import finity.fini.domain.DepositProduct;
import finity.fini.domain.ProductBase;
import finity.fini.domain.ProductPopularity;
import finity.fini.domain.SavingProduct;
import finity.fini.dto.Popularity.NaverDataLabRequestDTO;
import finity.fini.dto.Popularity.NaverDataLabResponseDTO;
import finity.fini.dto.Popularity.NaverSearchDTO;
import finity.fini.repository.DepositProductRepository;
import finity.fini.repository.ProductPopularityRepository;
import finity.fini.repository.SavingProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled; // [중요]
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchRankingService {
    private final SavingProductRepository savingProductRepository;
    private final DepositProductRepository depositProductRepository;
    private final ProductPopularityRepository popularityRepository;
    private final WebClient.Builder webClientBuilder; // (Bean으로 등록되어 있어야 함)

    @Value("${naver.api.client-id}")
    private String naverClientId;
    @Value("${naver.api.client-secret}")
    private String naverClientSecret;
    @Value("${gemini.api.key}") // [Gemini 키 주입]
    private String geminiApiKey;

    /**
     * [수정] 페이징 + 병렬 스트림 방식으로 변경
     */
    @Scheduled(cron = "0 43 20 * * ?") // (오후 8시 43분)
    @Transactional
    public void updatePopularityRank() {
        log.info("### V2 (Paging + Parallel) 배치 작업을 시작합니다. ###");

        // --- 1. 적금 상품 처리 (50개씩 페이징) ---
        Pageable pageable = PageRequest.of(0, 50); // 0페이지, 50개 단위
        Page<SavingProduct> savingPage;

        do {
            savingPage = savingProductRepository.findAll(pageable);
            log.info("적금 상품 처리 중... Page {}/{}", savingPage.getNumber() + 1, savingPage.getTotalPages());

            // [속도 개선] 50개 상품을 병렬 스트림으로 동시 처리
            savingPage.getContent().parallelStream().forEach(product -> {
                processProduct((ProductBase) product); // 헬퍼 메서드 호출
            });

            pageable = savingPage.nextPageable(); // 다음 페이지

        } while (savingPage.hasNext()); // 다음 페이지가 있으면 계속

        // --- 2. 예금 상품 처리 (50개씩 페이징) ---
        pageable = PageRequest.of(0, 50); // 0페이지, 50개 단위
        Page<DepositProduct> depositPage;

        do {
            depositPage = depositProductRepository.findAll(pageable);
            log.info("예금 상품 처리 중... Page {}/{}", depositPage.getNumber() + 1, depositPage.getTotalPages());

            // [속도 개선] 50개 상품을 병렬 스트림으로 동시 처리
            depositPage.getContent().parallelStream().forEach(product -> {
                processProduct((ProductBase) product); // 헬퍼 메서드 호출
            });

            pageable = depositPage.nextPageable();

        } while (depositPage.hasNext());

        log.info("### V2 배치 작업을 완료했습니다. ###");
    }

    /**
     * [신규 헬퍼 메서드]
     * 상품 1개를 받아서 API 호출 3번(뉴스, 데이터랩, Gemini) 및 DB 저장을 처리합니다.
     * 이 메서드는 병렬 스트림에 의해 여러 스레드에서 동시에 호출됩니다.
     */
    private void processProduct(ProductBase product) {
        String keyword = product.getBank().getKorCoNm() + " " + product.getFinPrdtNm();
        try {
            // 1. [Naver 뉴스 API] (랭킹 점수 + RAG 스니펫 확보)
            NaverSearchDTO newsResult = callNaverNewsApi(keyword);
            int newsCount = (newsResult.getTotal() != null) ? newsResult.getTotal() : 0;
            List<String> newsSnippets = newsResult.getItems().stream()
                    .map(NaverSearchDTO.Item::getDescription)
                    .limit(3) // RAG에 사용할 뉴스 3개
                    .toList();

            // 2. [Naver DataLab API] (랭킹 점수)
            double searchScore = callNaverDataLabApi(keyword);

            // 3. [최종 점수 계산] (가중치 조절 가능)
            double finalScore = (searchScore * 1.0) + (newsCount * 0.5);

            // 4. [RAG] AI 요약 멘트 생성
            String productInfo = "우대조건: " + product.getSpclCnd();
            String prompt = buildPrompt(product.getFinPrdtNm(), productInfo, newsSnippets);
            String aiSummary = callGeminiApi(prompt);

            // 5. [DB 저장]
            ProductPopularity pop = ProductPopularity.builder()
                    .productId(product.getProductId())
                    .productType(product.getProductTypeEnum())
                    .popularityScore(finalScore)
                    .aiSummary(aiSummary) // AI 요약 멘트 저장
                    .build();
            popularityRepository.save(pop);

            // [속도 개선] 불필요한 0.1초 대기 삭제!
            // Thread.sleep(100);

        } catch (Exception e) {
            log.error("상품 랭킹 업데이트 실패: {} - {}", keyword, e.getMessage());
        }
    }

    /**
     * [수정] Naver 뉴스 API (뉴스 스니펫도 가져오도록 display=5)
     */
    private NaverSearchDTO callNaverNewsApi(String keyword) {
        WebClient webClient = webClientBuilder.baseUrl("https://openapi.naver.com").build();
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/search/news.json")
                        .queryParam("query", keyword)
                        .queryParam("display", 5) // RAG를 위해 5개 정도 가져옴
                        .build())
                .header("X-Naver-Client-Id", naverClientId)
                .header("X-Naver-Client-Secret", naverClientSecret)
                .retrieve()
                .bodyToMono(NaverSearchDTO.class)
                .block();
    }

    /**
     * [신규] Naver DataLab API (POST)
     */
    private double callNaverDataLabApi(String keyword) {
        WebClient webClient = webClientBuilder.baseUrl("https://openapi.naver.com").build();

        // 최근 30일간 검색량 조회
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
                .bodyValue(requestBody) // DTO를 JSON 바디에 실어 보냄
                .retrieve()
                .bodyToMono(NaverDataLabResponseDTO.class)
                .block();

        // 30일간의 검색량(ratio)을 모두 합산하여 점수화
        if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
            return response.getResults().get(0).getData().stream()
                    .mapToDouble(NaverDataLabResponseDTO.Data::getRatio)
                    .sum();
        }
        return 0.0;
    }

    /**
     * [신규] RAG 프롬프트 빌더
     */
    private String buildPrompt(String productName, String productInfo, List<String> newsSnippets) {
        String newsText = newsSnippets.stream()
                .map(s -> "- " + s.replaceAll("<[^>]*>", "")) // HTML 태그 제거
                .collect(Collectors.joining("\n"));

        return String.format(
                """
                당신은 사용자의 쉬운 이해를 돕는 친절한 금융 상품 캐스터입니다.
                다음 [상품 정보]와 [최신 뉴스 동향]을 바탕으로, 이 상품이 왜 인기 있는지 또는 왜 주목해야 하는지 1~2줄의 매력적인 추천 멘트를 생성해 주세요.
                
                [상품 정보]
                - 상품명: %s
                - %s
                
                [최신 뉴스 동향]
                %s
                
                추천 멘트:
                """, productName, productInfo, newsText.isBlank() ? "최신 뉴스 없음" : newsText
        );
    }

    /**
     * [신규] Gemini API 호출
     */
    private String callGeminiApi(String prompt) {
        WebClient webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();

        String url = "/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

        // Gemini API 요청 바디 (간단한 Map 사용)
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        // Gemini API 응답 파싱 (Map 사용)
        Map<String, Object> response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class) // Map으로 받음
                .block();

        // Map에서 텍스트 결과 파싱 (매우 복잡한 구조)
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패", e);
            return "추천 정보를 생성 중입니다.";
        }
    }
}