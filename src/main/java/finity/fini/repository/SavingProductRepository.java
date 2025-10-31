package finity.fini.repository;

import finity.fini.domain.SavingProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingProductRepository extends JpaRepository<SavingProduct, Long>, JpaSpecificationExecutor<SavingProduct> {

}