package finity.fini.service.Product;

import finity.fini.dto.Product.ProductResponseDTO;
import org.springframework.data.domain.Page;

public interface ProductService {

    // 금융감독원 적금 상품 정보 동기화
    void syncSavingProducts();

    // 금융감독원 예금 상품 정보 동기화
    void syncDepositProducts();

    // 적금 상품 목록 조회
    Page<ProductResponseDTO.ProductListDTO> findSavingProducts(String bankCodes, Integer term, String mtrtCondition, int page);

    // 예금 상품 목록 조회
    Page<ProductResponseDTO.ProductListDTO> findDepositProducts(String bankCodes, Integer term, String mtrtCondition, int page);

    // 적금 상품 상세 조회
    ProductResponseDTO.ProductDetailDTO getSavingProductDetail(Long productId);

    // 예금 상품 상세 조회
    ProductResponseDTO.ProductDetailDTO getDepositProductDetail(Long productId);
}
