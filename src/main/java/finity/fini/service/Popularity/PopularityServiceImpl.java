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
        Pageable topSix = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "popularityScore"));
        List<ProductPopularity> popList = popularityRepository.findByProductType(ProductPopularity.ProductType.SAVING, topSix);

        List<ProductPopularity> comparisonList = popList.stream()
                .filter(p -> !p.getProductId().equals(currentProductId))
                .limit(5)
                .collect(Collectors.toList());
        return convertToPopularDTO(comparisonList, ProductPopularity.ProductType.SAVING);
    }

    @Override
    public List<ProductResponseDTO.PopularProductDTO> findDepositProductComparisons(Long currentProductId) {
        Pageable topSix = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "popularityScore"));
        List<ProductPopularity> popList = popularityRepository.findByProductType(ProductPopularity.ProductType.DEPOSIT, topSix);

        List<ProductPopularity> comparisonList = popList.stream()
                .filter(p -> !p.getProductId().equals(currentProductId))
                .limit(5)
                .collect(Collectors.toList());
        return convertToPopularDTO(comparisonList, ProductPopularity.ProductType.DEPOSIT);
    }

    /**
     * [헬퍼 메서드]
     * ProductPopularity 리스트를 DTO 리스트로 변환 (N+1 문제 방지)
     */
    private List<ProductResponseDTO.PopularProductDTO> convertToPopularDTO(
            List<ProductPopularity> popList, ProductPopularity.ProductType type) {

        if (popList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = popList.stream().map(ProductPopularity::getProductId).toList();

        Map<Long, ? extends ProductBase> productMap;
        if (type == ProductPopularity.ProductType.SAVING) {
            productMap = savingProductRepository.findAllById(productIds).stream()
                    .collect(Collectors.toMap(SavingProduct::getProductId, p -> p));
        } else {
            productMap = depositProductRepository.findAllById(productIds).stream()
                    .collect(Collectors.toMap(DepositProduct::getProductId, p -> p));
        }

        return popList.stream()
                .map(pop -> {
                    ProductBase product = productMap.get(pop.getProductId());
                    if (product == null) return null;

                    double maxRate = 0.0;
                    if (type == ProductPopularity.ProductType.SAVING) {
                        maxRate = ((SavingProduct) product).getSavingOptions().stream()
                                .mapToDouble(opt -> opt.getIntrRate2() != null ? opt.getIntrRate2() : 0.0)
                                .max().orElse(0.0);
                    } else {
                        maxRate = ((DepositProduct) product).getDepositOptions().stream()
                                .mapToDouble(opt -> opt.getIntrRate2() != null ? opt.getIntrRate2() : 0.0)
                                .max().orElse(0.0);
                    }

                    return ProductResponseDTO.PopularProductDTO.builder()
                            .productId(pop.getProductId())
                            .aiSummary(pop.getAiSummary()) // V2 기준 (AI 요약 포함)
                            .bankName(product.getBank().getKorCoNm())
                            .productName(product.getFinPrdtNm())
                            .maxRate(maxRate)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}