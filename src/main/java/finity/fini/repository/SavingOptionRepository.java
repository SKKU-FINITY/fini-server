package finity.fini.repository;

import finity.fini.domain.SavingOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SavingOptionRepository extends JpaRepository<SavingOption, Long>, JpaSpecificationExecutor<SavingOption> {
}
