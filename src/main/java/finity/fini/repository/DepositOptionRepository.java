package finity.fini.repository;

import finity.fini.domain.DepositOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DepositOptionRepository extends JpaRepository<DepositOption, Long>, JpaSpecificationExecutor<DepositOption> {

    @Query(value = "SELECT o FROM DepositOption o " +
            "JOIN FETCH o.depositProduct p " +
            "JOIN FETCH p.bank " +
            "WHERE o.depositOptionId <> :targetId " +
            "ORDER BY (ABS(o.intrRate - :baseRate) + ABS(o.intrRate2 - :maxRate)) ASC " +
            "LIMIT 5")
    List<DepositOption> findTop5SimilarProducts(@Param("targetId") Long targetId,
                                                @Param("baseRate") Double baseRate,
                                                @Param("maxRate") Double maxRate);
}
