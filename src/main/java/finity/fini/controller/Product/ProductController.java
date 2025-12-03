package finity.fini.controller.Product;

import finity.fini.apiPayload.ApiResponse;
import finity.fini.dto.Product.ProductResponseDTO;
import finity.fini.service.Product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Operation(summary = "적금 상품 목록 조회 API", description = "필터 조건에 맞는 적금 상품 목록을 조회합니다.")
    @Parameters({

            @Parameter(name = "bankNames", description = "은행 이름 목록 (여러 개 선택 가능)"),
            @Parameter(name = "term", description = "저축 희망 기간 목록 (단일 숫자, 입력한 개월 수 이하 상품 조회)"),
    })
    public ApiResponse<List<ProductResponseDTO.ProductListDTO>> getSavingProducts(
            @RequestParam(required = false) List<String> bankNames,
            @RequestParam(required = false) Integer term) {

        List<ProductResponseDTO.ProductListDTO> productList = productService.findSavingProducts(bankNames, term);
        return ApiResponse.onSuccess(productList);
    }

    @GetMapping("/deposits")
    @Operation(summary = "예금 상품 목록 조회 API", description = "필터 조건에 맞는 예금 상품 목록을 조회합니다.")
    @Parameters({
            @Parameter(name = "bankNames", description = "은행 이름 목록 (여러 개 선택 가능)"),
            @Parameter(name = "term", description = "저축 희망 기간 목록 (단일 숫자, 입력한 개월 수 이하 상품 조회)"),
    })
    public ApiResponse<List<ProductResponseDTO.ProductListDTO>> getDepositProducts(
            @RequestParam(required = false) List<String> bankNames,
            @RequestParam(required = false) Integer term) {

        List<ProductResponseDTO.ProductListDTO> productList = productService.findDepositProducts(bankNames, term);
        return ApiResponse.onSuccess(productList);
    }

    @GetMapping("/savings/{productId}/{optionId}")
    @Operation(summary = "특정 적금 상품 상세 조회 API", description = "상품 ID와 옵션 ID로 특정 적금 상품의 상세 정보를 조회합니다.")
    public ApiResponse<ProductResponseDTO.ProductDetailDTO> getSavingProductDetail(
            @PathVariable Long productId,
            @PathVariable Long optionId) {

        ProductResponseDTO.ProductDetailDTO productDetail = productService.getSavingProductDetail(productId, optionId);
        return ApiResponse.onSuccess(productDetail);
    }

    @GetMapping("/deposits/{productId}/{optionId}")
    @Operation(summary = "특정 예금 상품 상세 조회 API", description = "상품 ID와 옵션 ID로 특정 예금 상품의 상세 정보를 조회합니다.")
    public ApiResponse<ProductResponseDTO.ProductDetailDTO> getDepositProductDetail(
            @PathVariable Long productId,
            @PathVariable Long optionId) {

        ProductResponseDTO.ProductDetailDTO productDetail = productService.getDepositProductDetail(productId, optionId);
        return ApiResponse.onSuccess(productDetail);
    }
}