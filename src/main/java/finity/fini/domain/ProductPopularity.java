package finity.fini.domain;

import finity.fini.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter // 배치 작업에서 점수 업데이트를 위해 Setter 허용
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
// 상품 ID와 타입을 복합 기본 키로 사용
@IdClass(ProductPopularityId.class)
public class ProductPopularity extends BaseEntity {

    @Id
    private Long productId;

    @Id
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    private Double popularityScore; // Naver API로 계산한 최종 인기 점수

    @Column(columnDefinition = "TEXT")
    private String aiSummary; // [AI 요약 멘트 필드 다시 추가]

    public enum ProductType { SAVING, DEPOSIT }
}