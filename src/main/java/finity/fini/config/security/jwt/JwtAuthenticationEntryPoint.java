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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.warn("[JwtAuthenticationEntryPoint] 인증 실패: {}", authException.getMessage());

        // 1. 응답 상태코드 설정 (401)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 2. 응답 타입 설정 (JSON + UTF-8)
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 3. ApiResponse 생성 (code: COMMON401)
        ApiResponse<Object> errorResponse = ApiResponse.onFailure(
                ErrorStatus._UNAUTHORIZED.getCode(),
                ErrorStatus._UNAUTHORIZED.getMessage(),
                null
        );

        // 4. JSON으로 변환하여 Response Body에 직접 쓰기
        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(responseBody);
    }
}