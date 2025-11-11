package finity.fini.service.Popularity; // (패키지 경로는 맞게 수정해주세요)

import finity.fini.dto.Product.ProductResponseDTO;
import java.util.List;

/**
 * 인기 상품/추천 상품 관련 비즈니스 로직을 처리하는 서비스
 */
public interface PopularityService {

    /**
     * 첫 화면: 외부 인기순(Naver) 상위 5개 적금 상품 조회
     */
    List<ProductResponseDTO.PopularProductDTO> findPopularSavingProducts();

    /**
     * 첫 화면: 외부 인기순(Naver) 상위 5개 예금 상품 조회
     */
    List<ProductResponseDTO.PopularProductDTO> findPopularDepositProducts();

    /**
     * 비교 추천: 현재 상품을 제외한 인기 상위 5개 적금 상품 조회
     */
    List<ProductResponseDTO.PopularProductDTO> findSavingProductComparisons(Long currentProductId);

    /**
     * 비교 추천: 현재 상품을 제외한 인기 상위 5개 예금 상품 조회
     */
    List<ProductResponseDTO.PopularProductDTO> findDepositProductComparisons(Long currentProductId);
}