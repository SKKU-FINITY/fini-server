package finity.fini.repository;

import finity.fini.domain.DepositOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepositOptionRepository extends JpaRepository<DepositOption, Long>, JpaSpecificationExecutor<DepositOption> {
}
