package finity.fini.repository;

import finity.fini.domain.SavingProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SavingProductRepository extends JpaRepository<SavingProduct, Long> {

    // [추가 1] 배치 서비스(BatchRankingService)용: 페이징 + JOIN FETCH
    @Query(value = "SELECT sp FROM SavingProduct sp JOIN FETCH sp.bank b",
            countQuery = "SELECT COUNT(sp) FROM SavingProduct sp")
    Page<SavingProduct> findAllWithBank(Pageable pageable);

    // [추가 2] API 서비스(PopularityServiceImpl)용: N+1 방지 (Bank + Options)
    @Query("SELECT DISTINCT sp FROM SavingProduct sp " +
            "JOIN FETCH sp.bank b " +
            "LEFT JOIN FETCH sp.savingOptions so " +
            "WHERE sp.savingProductId IN :ids") // [수정] sp.id -> sp.savingProductId
    List<SavingProduct> findAllWithBankByIdIn(@Param("ids") List<Long> ids);

    // [추가 3] 상품 상세 비교를 위한 쿼리 (Bank, Options 모두 JOIN FETCH)
    @Query("SELECT sp FROM SavingProduct sp " +
            "JOIN FETCH sp.bank b " +
            "LEFT JOIN FETCH sp.savingOptions so " +
            "WHERE sp.savingProductId = :id") // [수정] sp.id -> sp.savingProductId
    List<SavingProduct> findWithDetailsById(@Param("id") Long id);
}