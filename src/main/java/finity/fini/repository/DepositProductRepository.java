package finity.fini.repository;

import finity.fini.domain.DepositProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositProductRepository extends JpaRepository<DepositProduct, Long> , JpaSpecificationExecutor<DepositProduct> {

}

