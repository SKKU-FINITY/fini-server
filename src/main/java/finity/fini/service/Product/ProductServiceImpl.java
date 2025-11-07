package finity.fini.service.Product;

import com.fasterxml.jackson.databind.ObjectMapper;
import finity.fini.converter.ProductConverter;
import finity.fini.domain.*;
import finity.fini.dto.Product.FssProductDTO;
import finity.fini.dto.Product.ProductResponseDTO;
import finity.fini.repository.BankRepository;
import finity.fini.repository.DepositProductRepository;
import finity.fini.repository.SavingProductRepository;
import finity.fini.repository.DepositOptionRepository;
import finity.fini.repository.SavingOptionRepository;
import finity.fini.domain.DepositOption;
import finity.fini.domain.SavingOption;
import org.springframework.data.domain.Sort;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

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

    @Value("${fss.api.key}")
    private String fssApiKey;

    @Value("${fss.api.url.savings}")
    private String fssSavingUrl;

    @Value("${fss.api.url.deposits}")
    private String fssDepositUrl;

    @Override
    @Transactional
    public void syncSavingProducts() {
        String url = String.format("%s?auth=%s&topFinGrpNo=020000&pageNo=1", fssSavingUrl, fssApiKey);
        log.info("Requesting FSS Savings API URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.info("금융감독원 [적금] API 응답 수신 완료. Status: {}", responseEntity.getStatusCode());

            if (responseEntity.getStatusCode() != HttpStatus.OK || !StringUtils.hasText(responseEntity.getBody())) {
                throw new RuntimeException("금융감독원 [적금] API로부터 비정상 응답을 받았습니다. Status: " + responseEntity.getStatusCode());
            }

            FssProductDTO fssProductDTO = objectMapper.readValue(responseEntity.getBody(), FssProductDTO.class);

            if (fssProductDTO == null || fssProductDTO.getResult() == null) {
                throw new RuntimeException("금융감독원 [적금] API 응답 파싱 후 데이터가 비어있습니다.");
            }

            List<FssProductDTO.BaseInfo> baseList = fssProductDTO.getResult().getBaseList();
            List<FssProductDTO.Option> optionList = fssProductDTO.getResult().getOptionList();

            Map<String, List<FssProductDTO.Option>> optionsByProduct = optionList.stream()
                    .collect(Collectors.groupingBy(opt -> opt.getFinCoNo() + "_" + opt.getFinPrdtCd()));

            for (FssProductDTO.BaseInfo baseInfo : baseList) {
                Bank bank = bankRepository.findByFinCoNo(baseInfo.getFinCoNo())
                        .orElseGet(() -> bankRepository.save(Bank.builder()
                                .finCoNo(baseInfo.getFinCoNo())
                                .korCoNm(baseInfo.getKorCoNm())
                                .dclsMonth(baseInfo.getDclsMonth())
                                .build()));

                SavingProduct product = ProductConverter.toSavingProduct(baseInfo, bank);

                List<FssProductDTO.Option> productOptions = optionsByProduct.get(baseInfo.getFinCoNo() + "_" + baseInfo.getFinPrdtCd());
                if (productOptions != null) {
                    List<SavingOption> options = productOptions.stream()
                            .map(opt -> ProductConverter.toSavingOption(opt, product))
                            .collect(Collectors.toList());
                    product.getSavingOptions().addAll(options);
                }
                savingProductRepository.save(product);
            }
            log.info("총 {}개의 [적금] 상품 정보 DB 저장 완료", baseList.size());

        } catch (Exception e) {
            log.error("금융감독원 [적금] API 동기화 중 오류 발생", e);
            throw new RuntimeException("금융감독원 [적금] API 동기화 처리 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional
    public void syncDepositProducts() {
        String url = String.format("%s?auth=%s&topFinGrpNo=020000&pageNo=1", fssDepositUrl, fssApiKey);
        log.info("Requesting FSS Deposits API URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.info("금융감독원 [예금] API 응답 수신 완료. Status: {}", responseEntity.getStatusCode());

            if (responseEntity.getStatusCode() != HttpStatus.OK || !StringUtils.hasText(responseEntity.getBody())) {
                throw new RuntimeException("금융감독원 [예금] API로부터 비정상 응답을 받았습니다. Status: " + responseEntity.getStatusCode());
            }

            FssProductDTO fssProductDTO = objectMapper.readValue(responseEntity.getBody(), FssProductDTO.class);

            if (fssProductDTO == null || fssProductDTO.getResult() == null) {
                throw new RuntimeException("금융감독원 [예금] API 응답 파싱 후 데이터가 비어있습니다.");
            }

            List<FssProductDTO.BaseInfo> baseList = fssProductDTO.getResult().getBaseList();
            List<FssProductDTO.Option> optionList = fssProductDTO.getResult().getOptionList();

            Map<String, List<FssProductDTO.Option>> optionsByProduct = optionList.stream()
                    .collect(Collectors.groupingBy(opt -> opt.getFinCoNo() + "_" + opt.getFinPrdtCd()));

            for (FssProductDTO.BaseInfo baseInfo : baseList) {
                Bank bank = bankRepository.findByFinCoNo(baseInfo.getFinCoNo())
                        .orElseGet(() -> bankRepository.save(Bank.builder()
                                .finCoNo(baseInfo.getFinCoNo())
                                .korCoNm(baseInfo.getKorCoNm())
                                .dclsMonth(baseInfo.getDclsMonth())
                                .build()));

                DepositProduct product = ProductConverter.toDepositProduct(baseInfo, bank);

                List<FssProductDTO.Option> productOptions = optionsByProduct.get(baseInfo.getFinCoNo() + "_" + baseInfo.getFinPrdtCd());
                if (productOptions != null) {
                    List<DepositOption> options = productOptions.stream()
                            .map(opt -> ProductConverter.toDepositOption(opt, product))
                            .collect(Collectors.toList());
                    product.getDepositOptions().addAll(options);
                }
                depositProductRepository.save(product);
            }
            log.info("총 {}개의 [예금] 상품 정보 DB 저장 완료", baseList.size());

        } catch (Exception e) {
            log.error("금융감독원 [예금] API 동기화 중 오류 발생", e);
            throw new RuntimeException("금융감독원 [예금] API 동기화 처리 중 오류가 발생했습니다.", e);
        }
    }


    @Override
    public List<ProductResponseDTO.ProductListDTO> findSavingProducts(List<String> bankNames, List<Integer> terms) {
        // 1. Specification<SavingOption> 생성 (옵션을 기준으로 조회)
        Specification<SavingOption> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // N+1 문제 방지를 위해 연관 엔티티(상품, 은행)를 fetch join 합니다.
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("savingProduct").fetch("bank");
            }

            // 은행 이름 필터링 (옵션 -> 상품 -> 은행 순으로 Join)
            if (bankNames != null && !bankNames.isEmpty()) {
                Join<SavingOption, SavingProduct> productJoin = root.join("savingProduct");
                predicates.add(productJoin.get("bank").get("korCoNm").in(bankNames));
            }

            // 저축 기간 필터링
            if (terms != null && !terms.isEmpty()) {
                predicates.add(root.get("saveTrm").in(terms));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 2. 정렬 조건 생성: 기본 금리(intrRate)
        Sort sort = Sort.by(Sort.Direction.DESC, "intrRate");

        // 3. DB에서 조건에 맞는 '옵션' 목록을 정렬된 상태로 조회
        List<SavingOption> options = savingOptionRepository.findAll(spec, sort);

        // 4. 조회된 옵션 목록을 DTO 목록으로 변환
        return options.stream()
                .map(ProductConverter::toProductListDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<ProductResponseDTO.ProductListDTO> findDepositProducts(List<String> bankNames, List<Integer> terms) {
        // 1. Specification<DepositOption> 생성
        Specification<DepositOption> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("depositProduct").fetch("bank");
            }

            if (bankNames != null && !bankNames.isEmpty()) {
                Join<DepositOption, DepositProduct> productJoin = root.join("depositProduct");
                predicates.add(productJoin.get("bank").get("korCoNm").in(bankNames));
            }

            if (terms != null && !terms.isEmpty()) {
                predicates.add(root.get("saveTrm").in(terms));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 2. 정렬 조건 생성: 기본 금리(intrRate)
        Sort sort = Sort.by(Sort.Direction.DESC, "intrRate");

        // 3. DB에서 '옵션' 목록을 정렬된 상태로 조회
        List<DepositOption> options = depositOptionRepository.findAll(spec, sort);

        // 4. DTO 목록으로 변환
        return options.stream()
                .map(ProductConverter::toProductListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    // [수정] optionId 파라미터 추가
    public ProductResponseDTO.ProductDetailDTO getSavingProductDetail(Long productId, Long optionId) {
        SavingProduct product = savingProductRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("해당 적금 상품을 찾을 수 없습니다. ID: " + productId));

        List<SavingOption> optionsToDisplay;

        if (optionId != null) {
            // [추가] optionId가 있으면, 해당 옵션만 필터링
            optionsToDisplay = product.getSavingOptions().stream()
                    .filter(opt -> optionId.equals(opt.getSavingOptionId()))
                    .collect(Collectors.toList());

            if (optionsToDisplay.isEmpty()) {
                throw new IllegalArgumentException(
                        "해당 옵션 ID는 이 상품에 속하지 않습니다. productId: " + productId + ", optionId: " + optionId);
            }
            // error handling 추가
        } else {
            // [기존] optionId가 없으면, 모든 옵션 포함
            optionsToDisplay = product.getSavingOptions();
        }

        // [수정] 필터링된 옵션 리스트를 컨버터로 전달
        return ProductConverter.toSavingProductDetailDTO(product, optionsToDisplay);
    }

    @Override
    @Transactional(readOnly = true)
    // [수정] optionId 파라미터 추가
    public ProductResponseDTO.ProductDetailDTO getDepositProductDetail(Long productId, Long optionId) {
        DepositProduct product = depositProductRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("해당 예금 상품을 찾을 수 없습니다. ID: " + productId));

        List<DepositOption> optionsToDisplay;

        if (optionId != null) {
            // [추가] optionId가 있으면, 해당 옵션만 필터링
            optionsToDisplay = product.getDepositOptions().stream()
                    .filter(opt -> optionId.equals(opt.getDepositOptionId()))
                    .collect(Collectors.toList());
        } else {
            // [기존] optionId가 없으면, 모든 옵션 포함
            optionsToDisplay = product.getDepositOptions();
        }

        // [수정] 필터링된 옵션 리스트를 컨버터로 전달
        return ProductConverter.toDepositProductDetailDTO(product, optionsToDisplay);
    }
}