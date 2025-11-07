package finity.fini.converter;

import finity.fini.domain.*;
import finity.fini.dto.Product.FssProductDTO;
import finity.fini.dto.Product.ProductResponseDTO;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ProductConverter {

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
                .finPrdtNm(baseInfo.getFinPrdtNm())
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
                .finPrdtNm(baseInfo.getFinPrdtNm())
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
        return ProductResponseDTO.ProductListDTO.builder()
                .productId(product.getSavingProductId())
                .optionId(option.getSavingOptionId()) // [추가] 옵션 ID 추가
                .bankName(product.getBank().getKorCoNm())
                .productName(product.getFinPrdtNm())
                .rsrvTypeNm(option.getRsrvTypeNm())
                .saveTerm(option.getSaveTrm())
                .baseRate(option.getIntrRate())
                .maxRate(option.getIntrRate2())
                .build();
    }

    /**
     * [수정] DepositOption 엔티티를 ProductListDTO로 변환합니다.
     */
    public static ProductResponseDTO.ProductListDTO toProductListDTO(DepositOption option) {
        DepositProduct product = option.getDepositProduct();
        return ProductResponseDTO.ProductListDTO.builder()
                .productId(product.getDepositProductId())
                .optionId(option.getDepositOptionId()) // [추가] 옵션 ID 추가
                .bankName(product.getBank().getKorCoNm())
                .productName(product.getFinPrdtNm())
                .saveTerm(option.getSaveTrm())
                .baseRate(option.getIntrRate())
                .maxRate(option.getIntrRate2())
                .build();
    }

    /**
     * [기존 메서드] - 모든 옵션을 표시 (하위 호환성용)
     */
    public static ProductResponseDTO.ProductDetailDTO toSavingProductDetailDTO(SavingProduct product) {
        return toSavingProductDetailDTO(product, product.getSavingOptions());
    }

    /**
     * [신규 오버로딩 메서드] - 선택된 옵션 리스트만 DTO로 변환합니다.
     */
    public static ProductResponseDTO.ProductDetailDTO toSavingProductDetailDTO(SavingProduct product, List<SavingOption> options) {
        // [수정] 매개변수로 받은 'options' 리스트를 사용
        List<ProductResponseDTO.OptionDTO> optionDTOs = options.stream()
                .map(option -> ProductResponseDTO.OptionDTO.builder()
                        .interestType(option.getIntrRateTypeNm())
                        .reserveType(option.getRsrvTypeNm())
                        .saveTerm(option.getSaveTrm())
                        .baseRate(option.getIntrRate())
                        .maxRate(option.getIntrRate2())
                        .build())
                .collect(Collectors.toList());

        return ProductResponseDTO.ProductDetailDTO.builder()
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
                .options(optionDTOs)
                .build();
    }

    public static ProductResponseDTO.ProductDetailDTO toDepositProductDetailDTO(DepositProduct product) {
        // [수정] 모든 옵션을 포함하여 새 메서드 호출
        return toDepositProductDetailDTO(product, product.getDepositOptions());
    }

    /**
     * [신규 오버로딩 메서드] - 선택된 옵션 리스트만 DTO로 변환합니다.
     */
    public static ProductResponseDTO.ProductDetailDTO toDepositProductDetailDTO(DepositProduct product, List<DepositOption> options) {
        // [수정] 매개변수로 받은 'options' 리스트를 사용
        List<ProductResponseDTO.OptionDTO> optionDTOs = options.stream()
                .map(option -> ProductResponseDTO.OptionDTO.builder()
                        .interestType(option.getIntrRateTypeNm())
                        .saveTerm(option.getSaveTrm())
                        .baseRate(option.getIntrRate())
                        .maxRate(option.getIntrRate2())
                        .build())
                .collect(Collectors.toList());

        return ProductResponseDTO.ProductDetailDTO.builder()
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
                .options(optionDTOs) // [수정] 필터링된 옵션 리스트가 반영됨
                .build();
    }

    /**
     * [추가] maxLimit(Long)을 가독성 있는 문자열로 변환합니다.
     * null이면 "제한 없음", 숫자면 콤마(,)가 포함된 문자열(예: "1,000,000")로 변환합니다.
     */
    private static String convertMaxLimit(Long maxLimit) {
        if (maxLimit == null) {
            return "제한 없음";
        }
        // 숫자 포맷팅 (예: 1000000 -> "1,000,000")
        return NumberFormat.getInstance(Locale.KOREA).format(maxLimit);
    }

    /**
     * join_deny 코드를 가독성 있는 문자열로 변환합니다.
     * (1:제한없음, 2:서민전용, 3:일부제한)
     * @param joinDenyCode DB에 저장된 코드 (1, 2, 3)
     * @return 변환된 문자열
     */
    private static String convertJoinDeny(String joinDenyCode) {
        if (joinDenyCode == null) {
            return null;
        }

        switch (joinDenyCode) {
            case "1":
                return "제한없음";
            case "2":
                return "서민전용";
            case "3":
                return "일부제한";
            default:
                return joinDenyCode;
        }
    }


}
