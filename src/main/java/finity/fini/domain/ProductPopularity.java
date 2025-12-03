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
    private String aiSummary;

    public enum ProductType { SAVING, DEPOSIT }

    public void updateData(Double popularityScore, String aiSummary) {
        this.popularityScore = popularityScore;
        // aiSummary가 null이 아닐 때만 업데이트 (기존 요약 유지 등 정책에 따라 조정 가능)
        if (aiSummary != null) {
            this.aiSummary = aiSummary;
        }
    }

    public void updateAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }
}