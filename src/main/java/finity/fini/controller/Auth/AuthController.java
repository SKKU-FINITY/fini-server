package finity.fini.controller.Auth;

import finity.fini.apiPayload.ApiResponse;
import finity.fini.converter.UserConverter;
import finity.fini.domain.User;
import finity.fini.dto.User.UserRequestDTO;
import finity.fini.dto.User.UserResponseDTO;
import finity.fini.service.Auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. 회원가입 API
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "회원가입을 진행합니다.")
    public ApiResponse<UserResponseDTO.RegisterResultDTO> register(@Valid @RequestBody UserRequestDTO.RegisterDTO request) {
        User newUser = authService.register(request);
        return ApiResponse.onSuccess(UserConverter.toRegisterResultDTO(newUser));
    }

    // 2. 로그인 API
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인 합니다.")
    public ApiResponse<UserResponseDTO.LoginResultDTO> login(@Valid @RequestBody UserRequestDTO.LoginDTO request) {
        String token = authService.login(request);
        return ApiResponse.onSuccess(UserConverter.toLoginResultDTO(token));
    }

    // 3. 내 정보 조회 API
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "내 정보를 조회합니다.")
    public ApiResponse<UserResponseDTO.MeResultDTO> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserResponseDTO.MeResultDTO response = authService.getMe(username);
        return ApiResponse.onSuccess(response);
    }

    // 4. 로그아웃 API
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃 합니다.")
    public ApiResponse<String> logout() {
        // Stateless JWT 방식에서는 서버에서 토큰을 직접 무효화하기 어렵습니다.
        // 클라이언트 측에서 토큰을 삭제하는 방식으로 로그아웃을 구현합니다.
        // 서버는 이 요청에 대해 성공 응답만 보내줍니다.
        return ApiResponse.onSuccess("로그아웃 되었습니다.");
    }
}