package finity.fini.controller.Popularity; // (패키지 경로는 맞게 수정해주세요)

import finity.fini.apiPayload.ApiResponse;
import finity.fini.dto.Product.ProductResponseDTO;
import finity.fini.service.Popularity.PopularityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations") // [경로 분리]
@RequiredArgsConstructor
@Tag(name = "Popularity", description = "인기 상품 추천 API")
public class PopularityController {

    private final PopularityService popularityService;

    @GetMapping("/savings")
    @Operation(summary = "인기 적금 상품 목록 API", description = "외부 인기순(Naver) 상위 5개 적금 상품을 조회합니다.")
    public ApiResponse<List<ProductResponseDTO.PopularProductDTO>> getPopularSavingProducts() {
        List<ProductResponseDTO.PopularProductDTO> productList = popularityService.findPopularSavingProducts();
        return ApiResponse.onSuccess(productList);
    }

    @GetMapping("/deposits")
    @Operation(summary = "인기 예금 상품 목록 API", description = "외부 인기순(Naver) 상위 5개 예금 상품을 조회합니다.")
    public ApiResponse<List<ProductResponseDTO.PopularProductDTO>> getPopularDepositProducts() {
        List<ProductResponseDTO.PopularProductDTO> productList = popularityService.findPopularDepositProducts();
        return ApiResponse.onSuccess(productList);
    }

    @GetMapping("/savings/compare/{productId}") // [경로 변경]
    @Operation(summary = "적금 비교 추천 API", description = "현재 상품을 제외한 적금 상품들을 인기도 내림차순으로 조회합니다.")
    public ApiResponse<List<ProductResponseDTO.PopularProductDTO>> getSavingProductComparisons(
            @PathVariable Long productId) {
        List<ProductResponseDTO.PopularProductDTO> productList = popularityService.findSavingProductComparisons(productId);
        return ApiResponse.onSuccess(productList);
    }

    @GetMapping("/deposits/compare/{productId}") // [경로 변경]
    @Operation(summary = "예금 비교 추천 API", description = "현재 상품을 제외한 예금 상품들을 인기도 내림차순으로 조회합니다.")
    public ApiResponse<List<ProductResponseDTO.PopularProductDTO>> getDepositProductComparisons(
            @PathVariable Long productId) {
        List<ProductResponseDTO.PopularProductDTO> productList = popularityService.findDepositProductComparisons(productId);
        return ApiResponse.onSuccess(productList);
    }
}