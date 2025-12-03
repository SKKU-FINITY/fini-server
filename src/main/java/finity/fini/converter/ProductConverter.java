package finity.fini.converter;

import finity.fini.domain.*;
import finity.fini.dto.Product.FssProductDTO;
import finity.fini.dto.Product.ProductResponseDTO;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ProductConverter {

    private static String cleanProductName(String rawName) {
        if (rawName == null) {
            return null;
        }
        // split 정규식을 사용하여 줄바꿈(\r 또는 \n)이 하나라도 있으면 기준으로 나눔
        String[] parts = rawName.split("[\\r\\n]+");

        // 첫 번째 부분(줄바꿈 앞부분)만 가져와서 공백 제거 후 반환
        return parts[0].trim();
    }

    /**
     * FSS API 응답(BaseInfo)을 SavingProduct 엔티티로 변환합니다. (DB 저장용)
     * @param baseInfo FSS API의 상품 기본 정보
     * @param bank     연관된 Bank 엔티티
     * @return SavingProduct 엔티티
     */
    public static SavingProduct toSavingProduct(FssProductDTO.BaseInfo baseInfo, Bank bank) {
        return SavingProduct.builder()
                .bank(bank)
                .finCoNo(baseInfo.getFinCoNo())
                .finPrdtCd(baseInfo.getFinPrdtCd())
                .finPrdtNm(cleanProductName(baseInfo.getFinPrdtNm()))
                .joinWay(baseInfo.getJoinWay())
                .mtrtInt(baseInfo.getMtrtInt())
                .spclCnd(baseInfo.getSpclCnd())
                .joinDeny(baseInfo.getJoinDeny())
                .joinMember(baseInfo.getJoinMember())
                .etcNote(baseInfo.getEtcNote())
                .maxLimit(baseInfo.getMaxLimit())
                .build();
    }

    /**
     * FSS API 응답(Option)을 SavingOption 엔티티로 변환합니다. (DB 저장용)
     * @param option  FSS API의 상품 옵션 정보
     * @param product 연관된 SavingProduct 엔티티
     * @return SavingOption 엔티티
     */
    public static SavingOption toSavingOption(FssProductDTO.Option option, SavingProduct product) {
        return SavingOption.builder()
                .savingProduct(product)
                .intrRateType(option.getIntrRateType())
                .intrRateTypeNm(option.getIntrRateTypeNm())
                .rsrvType(option.getRsrvType())
                .rsrvTypeNm(option.getRsrvTypeNm())
                .saveTrm(Integer.parseInt(option.getSaveTrm()))
                .intrRate(option.getIntrRate())
                .intrRate2(option.getIntrRate2())
                .build();
    }

    /**
     * FSS API 응답(BaseInfo)을 DepositProduct 엔티티로 변환합니다. (DB 저장용)
     * @param baseInfo FSS API의 상품 기본 정보
     * @param bank     연관된 Bank 엔티티
     * @return DepositProduct 엔티티
     */
    public static DepositProduct toDepositProduct(FssProductDTO.BaseInfo baseInfo, Bank bank) {
        return DepositProduct.builder()
                .bank(bank)
                .finCoNo(baseInfo.getFinCoNo())
                .finPrdtCd(baseInfo.getFinPrdtCd())
                .finPrdtNm(cleanProductName(baseInfo.getFinPrdtNm()))
                .joinWay(baseInfo.getJoinWay())
                .mtrtInt(baseInfo.getMtrtInt())
                .spclCnd(baseInfo.getSpclCnd())
                .joinDeny(baseInfo.getJoinDeny())
                .joinMember(baseInfo.getJoinMember())
                .etcNote(baseInfo.getEtcNote())
                .maxLimit(baseInfo.getMaxLimit())
                .build();
    }

    /**
     * FSS API 응답(Option)을 DepositOption 엔티티로 변환합니다. (DB 저장용)
     * @param option  FSS API의 상품 옵션 정보
     * @param product 연관된 DepositProduct 엔티티
     * @return DepositOption 엔티티
     */
    public static DepositOption toDepositOption(FssProductDTO.Option option, DepositProduct product) {
        return DepositOption.builder()
                .depositProduct(product)
                .intrRateType(option.getIntrRateType())
                .intrRateTypeNm(option.getIntrRateTypeNm())
                .saveTrm(Integer.parseInt(option.getSaveTrm()))
                .intrRate(option.getIntrRate())
                .intrRate2(option.getIntrRate2())
                .build();
    }


    public static ProductResponseDTO.ProductListDTO toProductListDTO(SavingOption option) {
        SavingProduct product = option.getSavingProduct();
        Double base = option.getIntrRate();
        Double max = option.getIntrRate2() != null ? option.getIntrRate2() : base;

        return ProductResponseDTO.ProductListDTO.builder()
                .productId(product.getSavingProductId())
                .optionId(option.getSavingOptionId())
                .bankName(product.getBank().getKorCoNm())
                .productName(product.getFinPrdtNm())
                .rsrvTypeNm(option.getRsrvTypeNm())
                .saveTerm(option.getSaveTrm())
                .baseRate(base)
                .maxRate(max)
                .build();
    }

    /**
     * [수정] DepositOption 엔티티를 ProductListDTO로 변환합니다.
     */
    public static ProductResponseDTO.ProductListDTO toProductListDTO(DepositOption option) {
        DepositProduct product = option.getDepositProduct();
        Double base = option.getIntrRate();
        Double max = option.getIntrRate2() != null ? option.getIntrRate2() : base;

        return ProductResponseDTO.ProductListDTO.builder()
                .productId(product.getDepositProductId())
                .optionId(option.getDepositOptionId()) // [추가] 옵션 ID 추가
                .bankName(product.getBank().getKorCoNm())
                .productName(product.getFinPrdtNm())
                .saveTerm(option.getSaveTrm())
                .baseRate(base)
                .maxRate(max)
                .build();
    }


    public static ProductResponseDTO.ProductDetailDTO toSavingProductDetailDTO(
            SavingProduct product,
            SavingOption option,
            List<ProductResponseDTO.SimilarProductDTO> similarProducts) {

        // null check 및 보정
        Double base = option.getIntrRate();
        // 최대 금리가 없으면 기본 금리와 동일하게 설정
        Double max = option.getIntrRate2() != null ? option.getIntrRate2() : base;

        return ProductResponseDTO.ProductDetailDTO.builder()
                // --- 기본 정보 ---
                .productId(product.getSavingProductId())
                .bankName(product.getBank().getKorCoNm())
                .productName(product.getFinPrdtNm())
                .joinDeny(convertJoinDeny(product.getJoinDeny()))
                .joinMember(product.getJoinMember())
                .joinWay(product.getJoinWay())
                .specialCondition(product.getSpclCnd())
                .etcNote(product.getEtcNote())
                .maxLimit(convertMaxLimit(product.getMaxLimit()))
                .maturityInterestInfo(product.getMtrtInt())

                // --- [수정] 옵션 정보 직접 매핑 ---
                .optionId(option.getSavingOptionId())
                .interestType(option.getIntrRateTypeNm())
                .reserveType(option.getRsrvTypeNm())
                .saveTerm(option.getSaveTrm())
                .baseRate(base)
                .maxRate(max)

                .similarProducts(similarProducts)
                .build();
    }

    /**
     * [수정] 예금 상세 DTO 변환 (옵션 평탄화 적용)
     */
    public static ProductResponseDTO.ProductDetailDTO toDepositProductDetailDTO(
            DepositProduct product,
            DepositOption option,
            List<ProductResponseDTO.SimilarProductDTO> similarProducts) {

        Double base = option.getIntrRate();
        Double max = option.getIntrRate2() != null ? option.getIntrRate2() : base;

        return ProductResponseDTO.ProductDetailDTO.builder()
                // --- 기본 정보 ---
                .productId(product.getDepositProductId())
                .bankName(product.getBank().getKorCoNm())
                .productName(product.getFinPrdtNm())
                .joinDeny(convertJoinDeny(product.getJoinDeny()))
                .joinMember(product.getJoinMember())
                .joinWay(product.getJoinWay())
                .specialCondition(product.getSpclCnd())
                .etcNote(product.getEtcNote())
                .maxLimit(convertMaxLimit(product.getMaxLimit()))
                .maturityInterestInfo(product.getMtrtInt())

                // --- [수정] 옵션 정보 직접 매핑 ---
                .optionId(option.getDepositOptionId())
                .interestType(option.getIntrRateTypeNm())
                // .reserveType() -> 예금은 적립방식 없음 (null)
                .saveTerm(option.getSaveTrm())
                .baseRate(base)
                .maxRate(max)

                .similarProducts(similarProducts)
                .build();
    }

    private static String convertMaxLimit(Long maxLimit) {
        if (maxLimit == null) {
            return "제한 없음";
        }
        return NumberFormat.getInstance(Locale.KOREA).format(maxLimit);
    }

    private static String convertJoinDeny(String joinDenyCode) {
        if (joinDenyCode == null) {
            return null;
        }
        switch (joinDenyCode) {
            case "1": return "제한없음";
            case "2": return "서민전용";
            case "3": return "일부제한";
            default: return joinDenyCode;
        }
    }

}
