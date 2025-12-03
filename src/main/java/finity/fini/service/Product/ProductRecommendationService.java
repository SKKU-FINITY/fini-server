package finity.fini.service.Product;

import finity.fini.domain.DepositOption;
import finity.fini.domain.SavingOption;
import finity.fini.dto.Product.ProductResponseDTO;

import java.util.List;

public interface ProductRecommendationService {

    List<ProductResponseDTO.SimilarProductDTO> getSimilarSavingProducts(SavingOption targetOption);

    List<ProductResponseDTO.SimilarProductDTO> getSimilarDepositProducts(DepositOption targetOption);


}
