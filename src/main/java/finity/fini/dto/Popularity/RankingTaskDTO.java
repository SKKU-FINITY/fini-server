package finity.fini.dto.Popularity;

import finity.fini.domain.ProductPopularity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankingTaskDTO {
    private Long productId;
    private ProductPopularity.ProductType productType;
    private String bankName;
    private String productName;
    private String specialCondition;
}