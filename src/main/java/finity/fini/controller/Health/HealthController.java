package finity.fini.controller.Health;

import finity.fini.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "헬스 체크 API")
@RestController
public class HealthController {

    @Operation(summary = "ALB Health Check", description = "ALB 헬스 체크용 API입니다.")
    @GetMapping("/") // 1. ALB 헬스 체크 경로
    public ApiResponse<String> healthCheck() {
        return ApiResponse.onSuccess("OK");
    }
}