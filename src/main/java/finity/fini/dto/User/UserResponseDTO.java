package finity.fini.dto.User;

import lombok.Builder;
import lombok.Getter;

public class UserResponseDTO {

    @Builder
    @Getter
    public static class RegisterResultDTO {
        private Long userId;
        private String username;
    }

    @Builder
    @Getter
    public static class LoginResultDTO {
        private String accessToken;
    }

    @Builder
    @Getter
    public static class MeResultDTO {
        private Long userId;
        private String username;
        private String email;
    }
}
