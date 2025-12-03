package finity.fini.service.Product;

import finity.fini.domain.DepositOption;
import finity.fini.domain.SavingOption;
import finity.fini.dto.Product.ProductResponseDTO;
import finity.fini.repository.DepositOptionRepository;
import finity.fini.repository.SavingOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductRecommendationServiceImpl implements ProductRecommendationService {

    private final SavingOptionRepository savingOptionRepository;
    private final DepositOptionRepository depositOptionRepository;

    public List<ProductResponseDTO.SimilarProductDTO> getSimilarSavingProducts(SavingOption targetOption) {
        Double baseRate = targetOption.getIntrRate() != null ? targetOption.getIntrRate() : 0.0;
        Double maxRate = targetOption.getIntrRate2() != null ? targetOption.getIntrRate2() : baseRate;

        List<SavingOption> similarOptions = savingOptionRepository.findTop5SimilarProducts(
                targetOption.getSavingOptionId(),
                baseRate,
                maxRate
        );

        return similarOptions.stream()
                .map(opt -> {
                    Long targetLimit = targetOption.getSavingProduct().getMaxLimit();
                    Long compareLimit = opt.getSavingProduct().getMaxLimit();

                    Double compBase = opt.getIntrRate() != null ? opt.getIntrRate() : 0.0;
                    Double compMax = opt.getIntrRate2() != null ? opt.getIntrRate2() : compBase;

                    return ProductResponseDTO.SimilarProductDTO.builder()
                            .productId(opt.getSavingProduct().getProductId())
                            .optionId(opt.getSavingOptionId())
                            .bankName(opt.getSavingProduct().getBank().getKorCoNm())
                            .productName(opt.getSavingProduct().getFinPrdtNm())

                            // [추가] 이자 유형 및 적립 방식 매핑
                            .interestType(opt.getIntrRateTypeNm())
                            .reserveType(opt.getRsrvTypeNm())

                            .termDiff(calculateIntDiff(opt.getSaveTrm(), targetOption.getSaveTrm()))
                            .baseRateDiff(calculateDoubleDiff(compBase, baseRate))
                            .maxRateDiff(calculateDoubleDiff(compMax, maxRate))
                            .maxLimitDiff(calculateLimitDiff(compareLimit, targetLimit))
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO.SimilarProductDTO> getSimilarDepositProducts(DepositOption targetOption) {
        Double baseRate = targetOption.getIntrRate() != null ? targetOption.getIntrRate() : 0.0;
        Double maxRate = targetOption.getIntrRate2() != null ? targetOption.getIntrRate2() : baseRate;

        List<DepositOption> similarOptions = depositOptionRepository.findTop5SimilarProducts(
                targetOption.getDepositOptionId(),
                baseRate,
                maxRate
        );

        return similarOptions.stream()
                .map(opt -> {
                    Long targetLimit = targetOption.getDepositProduct().getMaxLimit();
                    Long compareLimit = opt.getDepositProduct().getMaxLimit();

                    Double compBase = opt.getIntrRate() != null ? opt.getIntrRate() : 0.0;
                    Double compMax = opt.getIntrRate2() != null ? opt.getIntrRate2() : compBase;

                    return ProductResponseDTO.SimilarProductDTO.builder()
                            .productId(opt.getDepositProduct().getProductId())
                            .optionId(opt.getDepositOptionId())
                            .bankName(opt.getDepositProduct().getBank().getKorCoNm())
                            .productName(opt.getDepositProduct().getFinPrdtNm())

                            // [추가] 이자 유형 매핑 (예금은 reserveType 없음 -> null)
                            .interestType(opt.getIntrRateTypeNm())

                            .termDiff(calculateIntDiff(opt.getSaveTrm(), targetOption.getSaveTrm()))
                            .baseRateDiff(calculateDoubleDiff(compBase, baseRate))
                            .maxRateDiff(calculateDoubleDiff(compMax, maxRate))
                            .maxLimitDiff(calculateLimitDiff(compareLimit, targetLimit))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Integer calculateIntDiff(Integer compare, Integer target) {
        int c = (compare == null) ? 0 : compare;
        int t = (target == null) ? 0 : target;
        return c - t;
    }

    private Double calculateDoubleDiff(Double compare, Double target) {
        double diff = compare - target;
        return Math.round(diff * 100.0) / 100.0;
    }

    private Long calculateLimitDiff(Long compare, Long target) {
        if (compare == null || target == null) return null;
        return compare - target;
    }
}