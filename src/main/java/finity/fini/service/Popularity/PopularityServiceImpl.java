package finity.fini.service.Popularity; // (패키지 경로는 맞게 수정해주세요)

import finity.fini.domain.*;
import finity.fini.dto.Product.ProductResponseDTO;
import finity.fini.repository.DepositProductRepository;
import finity.fini.repository.ProductPopularityRepository;
import finity.fini.repository.SavingProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularityServiceImpl implements PopularityService {

    // [인기 상품 추천 로직에 필요한 Repository 3개]
    private final ProductPopularityRepository popularityRepository;
    private final SavingProductRepository savingProductRepository;
    private final DepositProductRepository depositProductRepository;

    @Override
    public List<ProductResponseDTO.PopularProductDTO> findPopularSavingProducts() {
        Pageable topFive = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "popularityScore"));
        List<ProductPopularity> popList = popularityRepository.findByProductType(ProductPopularity.ProductType.SAVING, topFive);
        return convertToPopularDTO(popList, ProductPopularity.ProductType.SAVING);
    }

    @Override
    public List<ProductResponseDTO.PopularProductDTO> findPopularDepositProducts() {
        Pageable topFive = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "popularityScore"));
        List<ProductPopularity> popList = popularityRepository.findByProductType(ProductPopularity.ProductType.DEPOSIT, topFive);
        return convertToPopularDTO(popList, ProductPopularity.ProductType.DEPOSIT);
    }

    @Override
    public List<ProductResponseDTO.PopularProductDTO> findSavingProductComparisons(Long currentProductId) {
        // [수정] 1. Pageable 제거, Repository의 새 메서드 호출
        List<ProductPopularity> comparisonList = popularityRepository
                .findAllByProductTypeAndProductIdNotOrderByPopularityScoreDesc(
                        ProductPopularity.ProductType.SAVING,
                        currentProductId
                );

        // [수정] 2. 인-메모리 필터링(.filter, .limit) 로직 제거
        return convertToPopularDTO(comparisonList, ProductPopularity.ProductType.SAVING);
    }

    @Override
    public List<ProductResponseDTO.PopularProductDTO> findDepositProductComparisons(Long currentProductId) {
        // [수정] 1. Pageable 제거, Repository의 새 메서드 호출
        List<ProductPopularity> comparisonList = popularityRepository
                .findAllByProductTypeAndProductIdNotOrderByPopularityScoreDesc(
                        ProductPopularity.ProductType.DEPOSIT,
                        currentProductId
                );

        // [수정] 2. 인-메모리 필터링(.filter, .limit) 로직 제거
        return convertToPopularDTO(comparisonList, ProductPopularity.ProductType.DEPOSIT);
    }


    private List<ProductResponseDTO.PopularProductDTO> convertToPopularDTO(
            List<ProductPopularity> popList, ProductPopularity.ProductType type) {

        if (popList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = popList.stream().map(ProductPopularity::getProductId).toList();

        Map<Long, ? extends ProductBase> productMap;
        if (type == ProductPopularity.ProductType.SAVING) {
            productMap = savingProductRepository.findAllWithBankByIdIn(productIds).stream()
                    .collect(Collectors.toMap(SavingProduct::getProductId, p -> p));
        } else {
            productMap = depositProductRepository.findAllWithBankByIdIn(productIds).stream()
                    .collect(Collectors.toMap(DepositProduct::getProductId, p -> p));
        }

        return popList.stream()
                .map(pop -> {
                    ProductBase product = productMap.get(pop.getProductId());
                    if (product == null) return null;

                    double maxRate = 0.0;
                    List<ProductResponseDTO.OptionDTO> options = Collections.emptyList(); // [추가]

                    if (type == ProductPopularity.ProductType.SAVING) {
                        SavingProduct sp = (SavingProduct) product;

                        // [수정] 옵션 DTO 리스트 생성
                        options = sp.getSavingOptions().stream()
                                .map(ProductResponseDTO.OptionDTO::from) // 팩토리 메서드 사용
                                .collect(Collectors.toList());

                        // [수정] 최고 금리 계산
                        maxRate = options.stream()
                                .mapToDouble(opt -> opt.getMaxRate() != null ? opt.getMaxRate() : 0.0)
                                .max().orElse(0.0);

                    } else {
                        DepositProduct dp = (DepositProduct) product;

                        // [수정] 옵션 DTO 리스트 생성
                        options = dp.getDepositOptions().stream()
                                .map(ProductResponseDTO.OptionDTO::from) // 팩토리 메서드 사용
                                .collect(Collectors.toList());

                        // [수정] 최고 금리 계산
                        maxRate = options.stream()
                                .mapToDouble(opt -> opt.getMaxRate() != null ? opt.getMaxRate() : 0.0)
                                .max().orElse(0.0);
                    }

                    return ProductResponseDTO.PopularProductDTO.builder()
                            .productId(pop.getProductId())
                            .aiSummary(pop.getAiSummary())
                            .bankName(product.getBank().getKorCoNm())
                            .productName(product.getFinPrdtNm())
                            .maxRate(maxRate)
                            .options(options) // [추가] DTO에 옵션 리스트 설정
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}