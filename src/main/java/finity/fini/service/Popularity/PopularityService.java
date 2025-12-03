package finity.fini.service.Popularity; // (패키지 경로는 맞게 수정해주세요)

import finity.fini.dto.Product.ProductResponseDTO;
import java.util.List;

/**
 * 인기 상품/추천 상품 관련 비즈니스 로직을 처리하는 서비스
 */
public interface PopularityService {

    List<ProductResponseDTO.PopularProductDTO> findPopularSavingProducts();

    List<ProductResponseDTO.PopularProductDTO> findPopularDepositProducts();

    List<ProductResponseDTO.PopularProductDTO> findSavingProductComparisons(Long currentProductId);

    List<ProductResponseDTO.PopularProductDTO> findDepositProductComparisons(Long currentProductId);
}