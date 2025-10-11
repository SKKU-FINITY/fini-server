package finity.fini.controller.Product;

import finity.fini.apiPayload.ApiResponse;
import finity.fini.dto.Product.ProductResponseDTO;
import finity.fini.service.Product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "금융상품 관련 API")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/sync")
    @Operation(summary = "금융상품 정보 동기화 API", description = "서버 DB에 최신 금융상품 정보를 동기화합니다. (관리자용)")
    public ApiResponse<String> syncProducts() {
        productService.syncSavingProducts();
        productService.syncDepositProducts();
        return ApiResponse.onSuccess("데이터 동기화가 성공적으로 완료되었습니다.");
    }

    @GetMapping("/savings")
    @Operation(summary = "적금 상품 목록 조회 API", description = "필터 조건에 맞는 적금 상품 목록을 페이징하여 조회합니다.")
    @Parameters({
            @Parameter(name = "bankCodes", description = "은행 고유 코드 목록 (쉼표로 구분)"),
            @Parameter(name = "term", description = "저축 희망 기간 (개월 단위)"),
            @Parameter(name = "mtrtCondition", description = "만기 후 이자율 조건 검색어"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)")
    })
    public ApiResponse<Page<ProductResponseDTO.ProductListDTO>> getSavingProducts(
            @RequestParam(required = false) String bankCodes,
            @RequestParam(required = false) Integer term,
            @RequestParam(required = false) String mtrtCondition,
            @RequestParam(defaultValue = "0") int page) {

        Page<ProductResponseDTO.ProductListDTO> productList = productService.findSavingProducts(bankCodes, term, mtrtCondition, page);
        return ApiResponse.onSuccess(productList);
    }

    @GetMapping("/deposits")
    @Operation(summary = "예금 상품 목록 조회 API", description = "필터 조건에 맞는 예금 상품 목록을 페이징하여 조회합니다.")
    @Parameters({
            @Parameter(name = "bankCodes", description = "은행 고유 코드 목록 (쉼표로 구분)"),
            @Parameter(name = "term", description = "저축 희망 기간 (개월 단위)"),
            @Parameter(name = "mtrtCondition", description = "만기 후 이자율 조건 검색어"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)")
    })
    public ApiResponse<Page<ProductResponseDTO.ProductListDTO>> getDepositProducts(
            @RequestParam(required = false) String bankCodes,
            @RequestParam(required = false) Integer term,
            @RequestParam(required = false) String mtrtCondition,
            @RequestParam(defaultValue = "0") int page) {

        Page<ProductResponseDTO.ProductListDTO> productList = productService.findDepositProducts(bankCodes, term, mtrtCondition, page);
        return ApiResponse.onSuccess(productList);
    }

    @GetMapping("/savings/{productId}")
    @Operation(summary = "특정 적금 상품 상세 조회 API", description = "상품 ID로 특정 적금 상품의 상세 정보를 조회합니다.")
    public ApiResponse<ProductResponseDTO.ProductDetailDTO> getSavingProductDetail(
            @PathVariable Long productId) {

        ProductResponseDTO.ProductDetailDTO productDetail = productService.getSavingProductDetail(productId);
        return ApiResponse.onSuccess(productDetail);
    }

    @GetMapping("/deposits/{productId}")
    @Operation(summary = "특정 예금 상품 상세 조회 API", description = "상품 ID로 특정 예금 상품의 상세 정보를 조회합니다.")
    public ApiResponse<ProductResponseDTO.ProductDetailDTO> getDepositProductDetail(
            @PathVariable Long productId) {

        ProductResponseDTO.ProductDetailDTO productDetail = productService.getDepositProductDetail(productId);
        return ApiResponse.onSuccess(productDetail);
    }
}