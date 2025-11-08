package finity.fini.repository;

import finity.fini.domain.ProductPopularity;
import finity.fini.domain.ProductPopularityId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductPopularityRepository extends JpaRepository<ProductPopularity, ProductPopularityId> {

    List<ProductPopularity> findByProductType(ProductPopularity.ProductType productType, Pageable pageable);
    /**
     * [추가된 메서드]
     * 배치 작업(BatchServiceImpl)에서 Upsert를 수행하기 위해
     * productId와 productType(복합키)으로 특정 상품의 인기 정보를 조회합니다.
     */
    Optional<ProductPopularity> findByProductIdAndProductType(Long productId, ProductPopularity.ProductType productType);
}
