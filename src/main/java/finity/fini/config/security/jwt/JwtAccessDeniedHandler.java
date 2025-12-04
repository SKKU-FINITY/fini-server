package finity.fini.config.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import finity.fini.apiPayload.ApiResponse;
import finity.fini.apiPayload.code.status.ErrorStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("[JwtAccessDeniedHandler] 권한 부족: {}", accessDeniedException.getMessage());

        // 1. 응답 상태코드 설정 (403)
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 2. 응답 타입 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 3. ApiResponse 생성 (code: COMMON403)
        ApiResponse<Object> errorResponse = ApiResponse.onFailure(
                ErrorStatus._FORBIDDEN.getCode(),
                ErrorStatus._FORBIDDEN.getMessage(),
                null
        );

        // 4. JSON 쓰기
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}