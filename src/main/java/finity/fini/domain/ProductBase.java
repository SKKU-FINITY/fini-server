package finity.fini.domain;

import finity.fini.domain.ProductPopularity.ProductType;
import jakarta.persistence.Transient;

public interface ProductBase {
    @Transient Long getProductId(); // 각 엔티티의 PK 반환
    @Transient ProductType getProductTypeEnum(); // "SAVING" 또는 "DEPOSIT" 반환
    Bank getBank();         // 은행명 접근용
    String getFinPrdtNm();  // 상품명 접근용
    String getSpclCnd();
}