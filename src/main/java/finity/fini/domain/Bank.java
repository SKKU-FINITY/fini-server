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
public class Bank extends BaseEntity {

    @Id
    @Column(name = "bank_id")
    private String finCoNo; // 금융회사 코드 (FSS 제공)

    private String korCoNm; // 금융회사 명
    private String dclsMonth; // 공시 제출월

    @Builder.Default
    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL)
    private List<SavingProduct> savingProducts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL)
    private List<DepositProduct> depositProducts = new ArrayList<>();
}