//package finity.fini.config.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        // "/", "/login", "/signup", "/css/**" 등의 경로는 로그인 없이 접근 허용
//                        .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**").permitAll()
//                        // 위에서 설정한 경로 외의 모든 경로는 인증을 요구
//                        .anyRequest().authenticated()
//                );
//
//        // 직접 만든 로그인 페이지를 사용하려면 아래와 같이 설정
//        http.formLogin(form -> form
//                .loginPage("/login") // 커스텀 로그인 페이지 경로
//                .permitAll()
//        );
//
//        http.csrf(csrf -> csrf.disable()); // 개발 중에는 CSRF를 비활성화하는 것이 편리
//
//        return http.build();
//    }
//}