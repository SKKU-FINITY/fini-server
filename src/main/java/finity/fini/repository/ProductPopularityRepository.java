package finity.fini.repository;

import finity.fini.domain.ProductPopularity;
import finity.fini.domain.ProductPopularityId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductPopularityRepository extends JpaRepository<ProductPopularity, ProductPopularityId> {

    List<ProductPopularity> findByProductType(ProductPopularity.ProductType productType, Pageable pageable);

    Optional<ProductPopularity> findByProductIdAndProductType(Long productId, ProductPopularity.ProductType productType);

    List<ProductPopularity> findAllByProductTypeAndProductIdNotOrderByPopularityScoreDesc(
            ProductPopularity.ProductType type,
            Long productId
    );
}
