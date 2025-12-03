package finity.fini.service.Product;

import com.fasterxml.jackson.databind.ObjectMapper;
import finity.fini.apiPayload.code.status.ErrorStatus;
import finity.fini.apiPayload.exception.handler.ProductHandler;
import finity.fini.converter.ProductConverter;
import finity.fini.domain.*;
import finity.fini.dto.Product.FssProductDTO;
import finity.fini.dto.Product.ProductResponseDTO;
import finity.fini.repository.*;
import finity.fini.domain.DepositOption;
import finity.fini.domain.SavingOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final BankRepository bankRepository;
    private final SavingProductRepository savingProductRepository;
    private final DepositProductRepository depositProductRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SavingOptionRepository savingOptionRepository;
    private final DepositOptionRepository depositOptionRepository;

    private final ProductRecommendationService recommendationService;
    private final ProductPopularityRepository popularityRepository;

    @Value("${fss.api.key}")
    private String fssApiKey;
    @Value("${fss.api.url.savings}")
    private String fssSavingUrl;
    @Value("${fss.api.url.deposits}")
    private String fssDepositUrl;

    // --- [적금] 동기화 로직 ---
    @Override
    @Transactional
    public void syncSavingProducts() {
        String url = String.format("%s?auth=%s&topFinGrpNo=020000&pageNo=1", fssSavingUrl, fssApiKey);
        processSync(url, true);
    }

    // --- [예금] 동기화 로직 ---
    @Override
    @Transactional
    public void syncDepositProducts() {
        String url = String.format("%s?auth=%s&topFinGrpNo=020000&pageNo=1", fssDepositUrl, fssApiKey);
        processSync(url, false);
    }

    // [통합] 동기화 처리 프로세스 (코드 중복 제거)
    private void processSync(String url, boolean isSaving) {
        log.info("Requesting FSS API URL: {}", url);

        // 1. API 호출
        FssProductDTO fssProductDTO = callFssApi(url);

        List<FssProductDTO.BaseInfo> baseList = fssProductDTO.getResult().getBaseList();
        List<FssProductDTO.Option> optionList = fssProductDTO.getResult().getOptionList();
        Map<String, List<FssProductDTO.Option>> optionsByProduct = optionList.stream()
                .collect(Collectors.groupingBy(opt -> opt.getFinCoNo() + "_" + opt.getFinPrdtCd()));

        // 2. 데이터 저장
        for (FssProductDTO.BaseInfo baseInfo : baseList) {
            Bank bank = bankRepository.findByFinCoNo(baseInfo.getFinCoNo())
                    .orElseGet(() -> bankRepository.save(Bank.builder()
                            .finCoNo(baseInfo.getFinCoNo())
                            .korCoNm(baseInfo.getKorCoNm())
                            .dclsMonth(baseInfo.getDclsMonth())
                            .build()));

            if (isSaving) {
                saveOrUpdateSavingProduct(baseInfo, bank, optionsByProduct.get(baseInfo.getFinCoNo() + "_" + baseInfo.getFinPrdtCd()));
            } else {
                saveOrUpdateDepositProduct(baseInfo, bank, optionsByProduct.get(baseInfo.getFinCoNo() + "_" + baseInfo.getFinPrdtCd()));
            }
        }
        log.info("총 {}개의 상품 정보 동기화 완료", baseList.size());
    }

    private void saveOrUpdateSavingProduct(FssProductDTO.BaseInfo baseInfo, Bank bank, List<FssProductDTO.Option> apiOptions) {
        SavingProduct product = savingProductRepository
                .findByFinCoNoAndFinPrdtCd(baseInfo.getFinCoNo(), baseInfo.getFinPrdtCd())
                .orElse(null);

        SavingProduct newInfo = ProductConverter.toSavingProduct(baseInfo, bank);

        if (product == null) {
            product = newInfo; // 새로 생성
        } else {
            // 기본 정보 업데이트
            product.setFinPrdtNm(newInfo.getFinPrdtNm());
            product.setJoinWay(newInfo.getJoinWay());
            product.setMtrtInt(newInfo.getMtrtInt());
            product.setSpclCnd(newInfo.getSpclCnd());
            product.setJoinDeny(newInfo.getJoinDeny());
            product.setJoinMember(newInfo.getJoinMember());
            product.setEtcNote(newInfo.getEtcNote());
            product.setMaxLimit(newInfo.getMaxLimit());
        }

        // [핵심] 옵션 리스트 초기화 및 중복 제거 후 추가
        if (product.getSavingOptions() == null) {
            product.setSavingOptions(new ArrayList<>());
        } else {
            product.getSavingOptions().clear(); // orphanRemoval=true면 여기서 Delete 쿼리 예약됨
        }

        if (apiOptions != null) {
            Set<String> uniqueKeys = new HashSet<>();
            for (FssProductDTO.Option opt : apiOptions) {
                // Key 생성: 공백 제거하여 비교 (더 강력한 중복 체크)
                String key = generateOptionKey(opt);
                if (uniqueKeys.add(key)) {
                    product.getSavingOptions().add(ProductConverter.toSavingOption(opt, product));
                }
            }
        }
        savingProductRepository.save(product);
    }

    private void saveOrUpdateDepositProduct(FssProductDTO.BaseInfo baseInfo, Bank bank, List<FssProductDTO.Option> apiOptions) {
        DepositProduct product = depositProductRepository
                .findByFinCoNoAndFinPrdtCd(baseInfo.getFinCoNo(), baseInfo.getFinPrdtCd())
                .orElse(null);

        DepositProduct newInfo = ProductConverter.toDepositProduct(baseInfo, bank);

        if (product == null) {
            product = newInfo;
        } else {
            product.setFinPrdtNm(newInfo.getFinPrdtNm());
            product.setJoinWay(newInfo.getJoinWay());
            product.setMtrtInt(newInfo.getMtrtInt());
            product.setSpclCnd(newInfo.getSpclCnd());
            product.setJoinDeny(newInfo.getJoinDeny());
            product.setJoinMember(newInfo.getJoinMember());
            product.setEtcNote(newInfo.getEtcNote());
            product.setMaxLimit(newInfo.getMaxLimit());
        }

        // [핵심] 옵션 리스트 초기화 및 중복 제거 후 추가
        if (product.getDepositOptions() == null) {
            product.setDepositOptions(new ArrayList<>());
        } else {
            product.getDepositOptions().clear(); // orphanRemoval=true면 여기서 Delete 쿼리 예약됨
        }

        if (apiOptions != null) {
            Set<String> uniqueKeys = new HashSet<>();
            for (FssProductDTO.Option opt : apiOptions) {
                String key = generateOptionKey(opt);
                if (uniqueKeys.add(key)) {
                    product.getDepositOptions().add(ProductConverter.toDepositOption(opt, product));
                }
            }
        }
        depositProductRepository.save(product);
    }

    // 중복 체크 키 생성 메서드
    private String generateOptionKey(FssProductDTO.Option opt) {
        String type = StringUtils.hasText(opt.getIntrRateType()) ? opt.getIntrRateType().replaceAll("\\s", "") : "null";
        String rsrv = StringUtils.hasText(opt.getRsrvType()) ? opt.getRsrvType().replaceAll("\\s", "") : "null";
        String term = StringUtils.hasText(opt.getSaveTrm()) ? opt.getSaveTrm().replaceAll("\\s", "") : "0";
        String rate1 = opt.getIntrRate() != null ? String.valueOf(opt.getIntrRate()) : "0.0";
        String rate2 = opt.getIntrRate2() != null ? String.valueOf(opt.getIntrRate2()) : "0.0";
        return type + "|" + rsrv + "|" + term + "|" + rate1 + "|" + rate2;
    }

    private FssProductDTO callFssApi(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() != HttpStatus.OK || !StringUtils.hasText(response.getBody())) {
                throw new ProductHandler(ErrorStatus.PRODUCT_SYNC_ERROR);
            }
            FssProductDTO dto = objectMapper.readValue(response.getBody(), FssProductDTO.class);
            if (dto == null || dto.getResult() == null) {
                throw new ProductHandler(ErrorStatus.PRODUCT_SYNC_ERROR);
            }
            return dto;
        } catch (Exception e) {
            log.error("FSS API 호출 중 오류: {}", url, e);
            throw new ProductHandler(ErrorStatus.PRODUCT_SYNC_ERROR);
        }
    }

    // --- 조회 메서드 (기존 유지) ---
    @Override
    public List<ProductResponseDTO.ProductListDTO> findSavingProducts(List<String> bankNames, Integer term) {
        Specification<SavingOption> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("savingProduct").fetch("bank");
            }
            if (bankNames != null && !bankNames.isEmpty()) {
                Join<SavingOption, SavingProduct> productJoin = root.join("savingProduct");
                predicates.add(productJoin.get("bank").get("korCoNm").in(bankNames));
            }
            if (term != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("saveTrm"), term));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Sort sort = Sort.by(Sort.Direction.DESC, "intrRate");
        List<SavingOption> options = savingOptionRepository.findAll(spec, sort);
        return options.stream().map(ProductConverter::toProductListDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO.ProductListDTO> findDepositProducts(List<String> bankNames, Integer term) {
        Specification<DepositOption> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("depositProduct").fetch("bank");
            }
            if (bankNames != null && !bankNames.isEmpty()) {
                Join<DepositOption, DepositProduct> productJoin = root.join("depositProduct");
                predicates.add(productJoin.get("bank").get("korCoNm").in(bankNames));
            }
            if (term != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("saveTrm"), term));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Sort sort = Sort.by(Sort.Direction.DESC, "intrRate");
        List<DepositOption> options = depositOptionRepository.findAll(spec, sort);
        return options.stream().map(ProductConverter::toProductListDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO.ProductDetailDTO getSavingProductDetail(Long productId, Long optionId) {
        SavingProduct product = savingProductRepository.findById(productId)
                .orElseThrow(() -> new ProductHandler(ErrorStatus.PRODUCT_NOT_FOUND));

        SavingOption targetOption = null;

        if (optionId != null) {
            targetOption = product.getSavingOptions().stream()
                    .filter(opt -> optionId.equals(opt.getSavingOptionId()))
                    .findFirst()
                    .orElseThrow(() -> new ProductHandler(ErrorStatus.PRODUCT_OPTION_NOT_FOUND));
        } else {
            if (!product.getSavingOptions().isEmpty()) {
                targetOption = product.getSavingOptions().get(0);
            } else {
                throw new ProductHandler(ErrorStatus.PRODUCT_OPTION_NOT_FOUND);
            }
        }

        List<ProductResponseDTO.SimilarProductDTO> similarProducts = recommendationService.getSimilarSavingProducts(targetOption);
        return ProductConverter.toSavingProductDetailDTO(product, targetOption, similarProducts);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO.ProductDetailDTO getDepositProductDetail(Long productId, Long optionId) {
        DepositProduct product = depositProductRepository.findById(productId)
                .orElseThrow(() -> new ProductHandler(ErrorStatus.PRODUCT_NOT_FOUND));

        DepositOption targetOption = null;

        if (optionId != null) {
            targetOption = product.getDepositOptions().stream()
                    .filter(opt -> optionId.equals(opt.getDepositOptionId()))
                    .findFirst()
                    .orElseThrow(() -> new ProductHandler(ErrorStatus.PRODUCT_OPTION_NOT_FOUND));
        } else {
            if (!product.getDepositOptions().isEmpty()) {
                targetOption = product.getDepositOptions().get(0);
            } else {
                throw new ProductHandler(ErrorStatus.PRODUCT_OPTION_NOT_FOUND);
            }
        }

        List<ProductResponseDTO.SimilarProductDTO> similarProducts = recommendationService.getSimilarDepositProducts(targetOption);
        return ProductConverter.toDepositProductDetailDTO(product, targetOption, similarProducts);
    }
}