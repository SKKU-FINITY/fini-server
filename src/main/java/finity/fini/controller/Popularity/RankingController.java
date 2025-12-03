package finity.fini.controller.Popularity;

import finity.fini.apiPayload.ApiResponse;
import finity.fini.service.Popularity.BatchRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
@Tag(name = "Ranking", description = "랭킹 및 AI 요약 관리 API")
public class RankingController {

    private final BatchRankingService batchRankingService;

    @PostMapping("/update")
    @Operation(summary = "인기 순위 및 AI 요약 강제 업데이트", description = "배치 작업을 수동으로 즉시 실행하여 랭킹 점수와 AI 요약을 갱신합니다.")
    public ApiResponse<String> manualUpdateRanking() {
        batchRankingService.updatePopularityRank();

        return ApiResponse.onSuccess("인기 순위 및 AI 요약 업데이트 작업이 완료되었습니다.");
    }
}