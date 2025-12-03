package finity.fini.domain;

import finity.fini.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_deposit_product",
                columnNames = {"fin_co_no", "fin_prdt_cd"}
        )
})
public class DepositProduct extends BaseEntity implements ProductBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long depositProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @Column(name = "fin_co_no")
    private String finCoNo;

    @Column(name = "fin_prdt_cd")
    private String finPrdtCd;

    private String finPrdtNm;

    private String joinWay;

    @Column(columnDefinition = "TEXT")
    private String mtrtInt;

    @Column(columnDefinition = "TEXT")
    private String spclCnd;

    private String joinDeny;

    private String joinMember;

    @Column(columnDefinition = "TEXT")
    private String etcNote;

    private Long maxLimit;

    @Builder.Default
    // [수정] orphanRemoval = true 추가: 리스트에서 제거되면 DB에서도 삭제됨 (중복 방지 핵심)
    @OneToMany(mappedBy = "depositProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DepositOption> depositOptions = new ArrayList<>();

    @Override
    @Transient // DB 컬럼이 아님을 명시
    public Long getProductId() {
        return this.depositProductId;
    }

    @Override
    @Transient
    public ProductPopularity.ProductType getProductTypeEnum() {
        return ProductPopularity.ProductType.DEPOSIT;
    }
}