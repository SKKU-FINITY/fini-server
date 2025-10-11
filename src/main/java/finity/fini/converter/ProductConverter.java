package finity.fini.converter;

import finity.fini.domain.*;
import finity.fini.dto.Product.FssProductDTO;
import finity.fini.dto.Product.ProductResponseDTO;

import java.util.List;
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
     * SavingProduct 엔티티를 상품 상세 조회 DTO로 변환합니다. (API 응답용)
     * @param product 조회된 SavingProduct 엔티티
     * @return ProductDetailDTO
     */
    public static ProductResponseDTO.ProductDetailDTO toSavingProductDetailDTO(SavingProduct product) {
        List<ProductResponseDTO.OptionDTO> optionDTOs = product.getSavingOptions().stream()
                .map(option -> ProductResponseDTO.OptionDTO.builder()
                        .interestType(option.getIntrRateTypeNm())
                        .reserveType(option.getRsrvTypeNm()) // 적금은 적립유형 포함
                        .saveTerm(option.getSaveTrm())
                        .baseRate(option.getIntrRate())
                        .maxRate(option.getIntrRate2())
                        .build())
                .collect(Collectors.toList());

        return ProductResponseDTO.ProductDetailDTO.builder()
                .productId(product.getSavingProductId())
                .bankName(product.getBank().getKorCoNm())
                .productName(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .specialCondition(product.getSpclCnd())
                .etcNote(product.getEtcNote())
                .maxLimit(product.getMaxLimit())
                .maturityInterestInfo(product.getMtrtInt())
                .options(optionDTOs)
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

    /**
     * DepositProduct 엔티티를 상품 상세 조회 DTO로 변환합니다. (API 응답용)
     * @param product 조회된 DepositProduct 엔티티
     * @return ProductDetailDTO
     */
    public static ProductResponseDTO.ProductDetailDTO toDepositProductDetailDTO(DepositProduct product) {
        List<ProductResponseDTO.OptionDTO> optionDTOs = product.getDepositOptions().stream()
                .map(option -> ProductResponseDTO.OptionDTO.builder()
                        .interestType(option.getIntrRateTypeNm())
                        // 예금은 적립유형(reserveType)이 없으므로 null
                        .saveTerm(option.getSaveTrm())
                        .baseRate(option.getIntrRate())
                        .maxRate(option.getIntrRate2())
                        .build())
                .collect(Collectors.toList());

        return ProductResponseDTO.ProductDetailDTO.builder()
                .productId(product.getDepositProductId())
                .bankName(product.getBank().getKorCoNm())
                .productName(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .specialCondition(product.getSpclCnd())
                .etcNote(product.getEtcNote())
                .maxLimit(product.getMaxLimit())
                .maturityInterestInfo(product.getMtrtInt())
                .options(optionDTOs)
                .build();
    }
}
