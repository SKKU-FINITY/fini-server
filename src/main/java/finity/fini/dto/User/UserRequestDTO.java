package finity.fini.dto.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class UserRequestDTO {

    @Getter
    public static class RegisterDTO {
        @NotBlank(message = "아이디는 필수입니다.")
        private String username;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;

        @Email(message = "유효한 이메일 형식이 아닙니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        private String email;
    }

    @Getter
    public static class LoginDTO {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }
}
