package finity.fini.service.Product;

import com.fasterxml.jackson.databind.ObjectMapper;
import finity.fini.converter.ProductConverter;
import finity.fini.domain.*;
import finity.fini.dto.Product.FssProductDTO;
import finity.fini.dto.Product.ProductResponseDTO;
import finity.fini.repository.BankRepository;
import finity.fini.repository.DepositProductRepository;
import finity.fini.repository.SavingProductRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public List<ProductResponseDTO.ProductListDTO> findSavingProducts(String bankCodes, Integer term, String mtrtCondition) {
        Specification<SavingProduct> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // N+1 문제 해결을 위한 Fetch Join 추가
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("savingOptions", JoinType.LEFT);
            }

            if (StringUtils.hasText(bankCodes)) {
                List<String> bankCodeList = Arrays.asList(bankCodes.split(","));
                predicates.add(root.get("bank").get("finCoNo").in(bankCodeList));
            }
            if (term != null) {
                // JOIN을 이미 했으므로 root.join 대신 root.get을 사용할 수 있습니다.
                predicates.add(criteriaBuilder.equal(root.get("savingOptions").get("saveTrm"), term));
            }
            if (StringUtils.hasText(mtrtCondition)) {
                predicates.add(criteriaBuilder.like(root.get("mtrtInt"), "%" + mtrtCondition + "%"));
            }

            query.distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<SavingProduct> productList = savingProductRepository.findAll(spec);

        return productList.stream().map(product -> {
            // 대표 옵션 선택 로직
            Optional<SavingOption> representativeOption;
            if (term != null) {
                // 기간으로 필터링한 경우 해당 기간의 옵션을 찾음
                representativeOption = product.getSavingOptions().stream()
                        .filter(opt -> term.equals(opt.getSaveTrm()))
                        .findFirst();
            } else {
                // 필터링이 없는 경우 최고 금리 옵션을 찾음
                representativeOption = product.getSavingOptions().stream()
                        .max(Comparator.comparing(SavingOption::getIntrRate2,
                                Comparator.nullsFirst(Comparator.naturalOrder())));
            }

            // 상품의 전체 옵션 중 최고 금리 계산
            double maxRate = product.getSavingOptions().stream()
                    .filter(opt -> opt.getIntrRate2() != null) // **이 필터가 핵심입니다.**
                    .mapToDouble(SavingOption::getIntrRate2)
                    .max()
                    .orElse(0.0);

            // DTO 빌더에 saveTerm과 baseRate 추가
            return ProductResponseDTO.ProductListDTO.builder()
                    .productId(product.getSavingProductId())
                    .bankName(product.getBank().getKorCoNm())
                    .productName(product.getFinPrdtNm())
                    .saveTerm(representativeOption.map(SavingOption::getSaveTrm).orElse(null))
                    .baseRate(representativeOption.map(SavingOption::getIntrRate).orElse(null))
                    .maxRate(maxRate)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO.ProductListDTO> findDepositProducts(String bankCodes, Integer term, String mtrtCondition) {
        Specification<DepositProduct> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // N+1 문제 해결을 위한 Fetch Join 추가
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("depositOptions", JoinType.LEFT);
            }

            if (StringUtils.hasText(bankCodes)) {
                List<String> bankCodeList = Arrays.asList(bankCodes.split(","));
                predicates.add(root.get("bank").get("finCoNo").in(bankCodeList));
            }
            if (term != null) {
                predicates.add(criteriaBuilder.equal(root.get("depositOptions").get("saveTrm"), term));
            }
            if (StringUtils.hasText(mtrtCondition)) {
                predicates.add(criteriaBuilder.like(root.get("mtrtInt"), "%" + mtrtCondition + "%"));
            }

            query.distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<DepositProduct> productList = depositProductRepository.findAll(spec);

        return productList.stream().map(product -> {
            // 대표 옵션 선택 로직
            Optional<DepositOption> representativeOption;
            if (term != null) {
                // 기간으로 필터링한 경우 해당 기간의 옵션을 찾음
                representativeOption = product.getDepositOptions().stream()
                        .filter(opt -> term.equals(opt.getSaveTrm()))
                        .findFirst();
            } else {
                // 필터링이 없는 경우 최고 금리 옵션을 찾음
                representativeOption = product.getDepositOptions().stream()
                        .max(Comparator.comparing(DepositOption::getIntrRate2,
                                Comparator.nullsFirst(Comparator.naturalOrder())));
            }

            // 상품의 전체 옵션 중 최고 금리 계산
            double maxRate = product.getDepositOptions().stream()
                    .filter(opt -> opt.getIntrRate2() != null) // **이 필터가 핵심입니다.**
                    .mapToDouble(DepositOption::getIntrRate2)
                    .max()
                    .orElse(0.0);


            // DTO 빌더에 saveTerm과 baseRate 추가
            return ProductResponseDTO.ProductListDTO.builder()
                    .productId(product.getDepositProductId())
                    .bankName(product.getBank().getKorCoNm())
                    .productName(product.getFinPrdtNm())
                    .saveTerm(representativeOption.map(DepositOption::getSaveTrm).orElse(null))
                    .baseRate(representativeOption.map(DepositOption::getIntrRate).orElse(null))
                    .maxRate(maxRate)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO.ProductDetailDTO getSavingProductDetail(Long productId) {
        SavingProduct product = savingProductRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("해당 적금 상품을 찾을 수 없습니다. ID: " + productId));
        return ProductConverter.toSavingProductDetailDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO.ProductDetailDTO getDepositProductDetail(Long productId) {
        DepositProduct product = depositProductRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("해당 예금 상품을 찾을 수 없습니다. ID: " + productId));
        return ProductConverter.toDepositProductDetailDTO(product);
    }
}