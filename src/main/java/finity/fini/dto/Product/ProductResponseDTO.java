package finity.fini.dto.Product;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class ProductResponseDTO {

    // 상품 목록 조회 응답 DTO
    @Builder
    @Getter
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
        private List<OptionDTO> options;
    }

    // 상세 조회 시 포함될 옵션 DTO
    @Builder
    @Getter
    public static class OptionDTO {
        private String interestType; // 단리/복리
        private String reserveType;  // 적립방식 (적금 전용)
        private Integer saveTerm;    // 저축 기간
        private Double baseRate;     // 저축 금리
        private Double maxRate;      // 최고 우대 금리
    }

    @Getter
    @Builder
    public static class PopularProductDTO {
        private Long productId;
        private String bankName;
        private String productName;
        private String aiSummary;
        private Double maxRate;     // 대표 최고 금리
    }
}
