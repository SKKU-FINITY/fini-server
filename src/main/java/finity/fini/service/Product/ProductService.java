package finity.fini.service.Product;

import finity.fini.dto.Product.ProductResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    // 금융감독원 적금 상품 정보 동기화
    void syncSavingProducts();

    // 금융감독원 예금 상품 정보 동기화
    void syncDepositProducts();

    // 적금 상품 목록 조회
    List<ProductResponseDTO.ProductListDTO> findSavingProducts(List<String> bankNames, List<Integer> terms);

    // 예금 상품 목록 조회
    List<ProductResponseDTO.ProductListDTO> findDepositProducts(List<String> bankNames, List<Integer> terms);

    // 적금 상품 상세 조회
    ProductResponseDTO.ProductDetailDTO getSavingProductDetail(Long productId, Long optionId);

    // 예금 상품 상세 조회
    ProductResponseDTO.ProductDetailDTO getDepositProductDetail(Long productId, Long optionId);
}
