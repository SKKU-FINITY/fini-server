package finity.fini.domain;

import finity.fini.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DepositOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long depositOptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_product_id")
    private DepositProduct depositProduct;

    private String intrRateType; // 저축 금리 유형
    private String intrRateTypeNm; // 저축 금리 유형명
    private Integer saveTrm; // 저축 기간 [단위: 개월]
    private Double intrRate; // 저축 금리 [소수점 2자리]
    private Double intrRate2; // 최고 우대금리 [소수점 2자리]
}
