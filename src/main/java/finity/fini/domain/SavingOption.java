package finity.fini.domain;

import finity.fini.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SavingOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savingOptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_product_id")
    private SavingProduct savingProduct;

    private String intrRateType;    // 저축 금리 유형 (S:단리, M:복리)
    private String intrRateTypeNm;  // 저축 금리 유형명
    private String rsrvType;        // 적립 유형 (S:자유적립식, F:정액적립식)
    private String rsrvTypeNm;      // 적립 유형명
    private Integer saveTrm;        // 저축 기간 (개월)
    private Double intrRate;        // 저축 금리
    private Double intrRate2;       // 최고 우대금리
}