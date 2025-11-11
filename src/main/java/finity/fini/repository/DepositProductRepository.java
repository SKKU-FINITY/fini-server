package finity.fini.repository;

import finity.fini.domain.DepositProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DepositProductRepository extends JpaRepository<DepositProduct, Long> {

    // [추가 1] 배치 서비스(BatchRankingService)용: 페이징 + JOIN FETCH
    @Query(value = "SELECT dp FROM DepositProduct dp JOIN FETCH dp.bank b",
            countQuery = "SELECT COUNT(dp) FROM DepositProduct dp")
    Page<DepositProduct> findAllWithBank(Pageable pageable);

    // [추가 2] API 서비스(PopularityServiceImpl)용: N+1 방지 (Bank + Options)
    @Query("SELECT DISTINCT dp FROM DepositProduct dp " +
            "JOIN FETCH dp.bank b " +
            "LEFT JOIN FETCH dp.depositOptions do " +
            "WHERE dp.depositProductId IN :ids") // [수정] dp.id -> dp.depositProductId
    List<DepositProduct> findAllWithBankByIdIn(@Param("ids") List<Long> ids);

    // [추가 3] 상품 상세 비교를 위한 쿼리 (Bank, Options 모두 JOIN FETCH)
    @Query("SELECT dp FROM DepositProduct dp " +
            "JOIN FETCH dp.bank b " +
            "LEFT JOIN FETCH dp.depositOptions do " +
            "WHERE dp.depositProductId = :id") // [수정] dp.id -> dp.depositProductId
    List<DepositProduct> findWithDetailsById(@Param("id") Long id);
}