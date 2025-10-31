package finity.fini.domain;

import finity.fini.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
// 금융회사코드와 상품코드는 유니크해야 함
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_saving_product",
                columnNames = {"fin_co_no", "fin_prdt_cd"}
        )
})
public class SavingProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savingProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @Column(name = "fin_co_no")
    private String finCoNo;     // 금융회사 코드

    @Column(name = "fin_prdt_cd")
    private String finPrdtCd;   // 금융상품 코드

    private String finPrdtNm;   // 금융 상품명

    private String joinWay;     // 가입 방법

    @Column(columnDefinition = "TEXT")
    private String mtrtInt;     // 만기 후 이자율

    @Column(columnDefinition = "TEXT")
    private String spclCnd;     // 우대조건

    private String joinDeny;    // 가입제한 (1:제한없음, 2:서민전용, 3:일부제한)

    private String joinMember;  // 가입대상

    @Column(columnDefinition = "TEXT")
    private String etcNote;     // 기타 유의사항

    private Long maxLimit;   // 최고한도

    // Initialize the list using @Builder.Default
    @Builder.Default
    @OneToMany(mappedBy = "savingProduct", cascade = CascadeType.ALL)
    private List<SavingOption> savingOptions = new ArrayList<>();
}