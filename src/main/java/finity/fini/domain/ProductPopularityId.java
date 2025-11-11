package finity.fini.domain;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode
public class ProductPopularityId implements Serializable {
    private Long productId;
    private ProductPopularity.ProductType productType;
}