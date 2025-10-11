package finity.fini.config.security.jwt;

import finity.fini.service.User.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String token = getTokenFromRequest(request);

        // 1. 요청 헤더에서 가져온 토큰이 유효한지 검사합니다.
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            // 2. 토큰이 유효하면, 토큰에서 username을 추출합니다.
            String username = jwtUtil.getUsername(token);

            // 3. username으로 UserDetails 객체를 조회합니다.
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 4. 조회된 UserDetails를 기반으로 인증(Authentication) 객체를 생성합니다.
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 5. 생성된 인증 객체를 SecurityContextHolder에 설정하여,
            //    해당 요청이 인증되었음을 시스템에 알립니다.
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 다음 필터로 요청을 전달합니다.
        filterChain.doFilter(request, response);
    }

    /**
     * HttpServletRequest의 헤더에서 'Bearer ' 접두사를 제거하고 순수한 JWT를 반환합니다.
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}