package finity.fini.dto.Product;

import com.fasterxml.jackson.annotation.JsonInclude;
import finity.fini.domain.DepositOption;
import finity.fini.domain.SavingOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ProductResponseDTO {

    // 상품 목록 조회 응답 DTO
    @Builder
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductListDTO {
        private Long productId;
        private Long optionId;
        private String bankName;
        private String productName;
        private String rsrvTypeNm;  // 적립 유형명
        private Integer saveTerm;   // 저축 기간
        private Double baseRate;    // 기본 금리
        private Double maxRate;     // 최고 우대금리
    }

    // 상품 상세 조회 응답 DTO
    @Builder
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductDetailDTO {
        private Long productId;
        private String bankName;
        private String productName;
        private String joinDeny;            // 가입제한
        private String joinMember;          // 가입대상
        private String joinWay;             // 가입 방법
        private String specialCondition;    // 우대 조건
        private String etcNote;             // 기타 유의사항
        private String maxLimit;            // 최고 한도
        private String maturityInterestInfo;// 만기 후 이자율

        private Long optionId;
        private String interestType; // 이자 계산 방식 (단리/복리)
        private String reserveType;  // 적립 방식 (자유적립/정액적립 - 적금용)
        private Integer saveTerm;    // 저축 기간
        private Double baseRate;     // 기본 금리
        private Double maxRate;      // 최고 우대 금리

        private List<SimilarProductDTO> similarProducts;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OptionDTO {
        private Long optionId;
        private String interestType; // 단리/복리 (Deposit)
        private String reserveType;  // 적립방식 (Saving)
        private Integer saveTerm;    // 저축 기간 (DTO 필드명)
        private Double baseRate;     // 저축 금리 (DTO 필드명)
        private Double maxRate;      // 최고 우대 금리 (DTO 필드명)

        public static OptionDTO from(SavingOption opt) {
            return OptionDTO.builder()
                    .optionId(opt.getSavingOptionId())
                    .reserveType(opt.getRsrvTypeNm())     // DTO.reserveType <- Entity.rsrvTypeNm
                    .saveTerm(opt.getSaveTrm())       // DTO.saveTerm <- Entity.saveTrm
                    .baseRate(opt.getIntrRate())      // DTO.baseRate <- Entity.intrRate
                    .maxRate(opt.getIntrRate2())      // DTO.maxRate <- Entity.intrRate2
                    .build();
        }

        public static OptionDTO from(DepositOption opt) {
            return OptionDTO.builder()
                    .optionId(opt.getDepositOptionId())
                    .interestType(opt.getIntrRateTypeNm()) // DTO.interestType <- Entity.intrRateTypeNm
                    .saveTerm(opt.getSaveTrm())        // DTO.saveTerm <- Entity.saveTrm
                    .baseRate(opt.getIntrRate())       // DTO.baseRate <- Entity.intrRate
                    .maxRate(opt.getIntrRate2())       // DTO.maxRate <- Entity.intrRate2
                    .build();
        }
    }

    @Builder
    @Getter
    public static class SimilarProductDTO {
        private Long productId;
        private Long optionId;
        private String bankName;
        private String productName;

        // 이자 유형 (단리/복리)
        private String interestType;

        //  적립 방식 (자유적립/정액적립) - 예금은 null이므로 제외
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String reserveType;

        private Long maxLimitDiff;   // 금액 차이 (Long)
        private Integer termDiff;    // 개월 차이 (Integer)
        private Double baseRateDiff; // 기본 금리 차이 (Double)
        private Double maxRateDiff;  // 최대 금리 차이 (Double)
    }

    @Getter
    @Builder
    public static class PopularProductDTO {
        private Long productId;
        private String bankName;
        private String productName;
        private String aiSummary;
        private Double maxRate;     // 대표 최고 금리
        private List<OptionDTO> options;
    }

}