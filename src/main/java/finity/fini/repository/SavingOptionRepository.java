package finity.fini.repository;

import finity.fini.domain.SavingOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SavingOptionRepository extends JpaRepository<SavingOption, Long>, JpaSpecificationExecutor<SavingOption> {

    // 금리 유사도 기반 상위 5개 추천 (자신 제외)
    // 유사도 공식: ABS(타겟기본 - 후보기본) + ABS(타겟최대 - 후보최대) 가 0에 가까울수록 유사함
    @Query(value = "SELECT o FROM SavingOption o " +
            "JOIN FETCH o.savingProduct p " +
            "JOIN FETCH p.bank " +
            "WHERE o.savingOptionId <> :targetId " +
            "ORDER BY (ABS(o.intrRate - :baseRate) + ABS(o.intrRate2 - :maxRate)) ASC " +
            "LIMIT 5")
    List<SavingOption> findTop5SimilarProducts(@Param("targetId") Long targetId,
                                               @Param("baseRate") Double baseRate,
                                               @Param("maxRate") Double maxRate);

}
