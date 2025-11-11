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

    // [수정됨] 상세 조회 및 인기 상품 목록에서 공통으로 사용할 OptionDTO
    @Builder
    @Getter
    @NoArgsConstructor  // [추가] from() 메서드의 .builder() 사용을 위해
    @AllArgsConstructor // [추가] from() 메서드의 .builder() 사용을 위해
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OptionDTO {
        private String interestType; // 단리/복리 (Deposit)
        private String reserveType;  // 적립방식 (Saving)
        private Integer saveTerm;    // 저축 기간 (DTO 필드명)
        private Double baseRate;     // 저축 금리 (DTO 필드명)
        private Double maxRate;      // 최고 우대 금리 (DTO 필드명)

        // [신규 추가] SavingOption -> DTO 변환 팩토리 메서드
        public static OptionDTO from(SavingOption opt) {
            return OptionDTO.builder()
                    .reserveType(opt.getRsrvTypeNm())     // DTO.reserveType <- Entity.rsrvTypeNm
                    .saveTerm(opt.getSaveTrm())       // DTO.saveTerm <- Entity.saveTrm
                    .baseRate(opt.getIntrRate())      // DTO.baseRate <- Entity.intrRate
                    .maxRate(opt.getIntrRate2())      // DTO.maxRate <- Entity.intrRate2
                    .build();
        }

        // [신규 추가] DepositOption -> DTO 변환 팩토리 메서드
        public static OptionDTO from(DepositOption opt) {
            return OptionDTO.builder()
                    .interestType(opt.getIntrRateTypeNm()) // DTO.interestType <- Entity.intrRateTypeNm
                    .saveTerm(opt.getSaveTrm())        // DTO.saveTerm <- Entity.saveTrm
                    .baseRate(opt.getIntrRate())       // DTO.baseRate <- Entity.intrRate
                    .maxRate(opt.getIntrRate2())       // DTO.maxRate <- Entity.intrRate2
                    .build();
        }
    }

    @Getter
    @Builder
    public static class PopularProductDTO {
        private Long productId;
        private String bankName;
        private String productName;
        private String aiSummary;
        private Double maxRate;     // 대표 최고 금리
        private List<OptionDTO> options; // [수정] 이제 위의 통합 OptionDTO를 사용합니다.
    }

    // [삭제] 중복되었던 두 번째 OptionDTO 클래스가 삭제되었습니다.
}