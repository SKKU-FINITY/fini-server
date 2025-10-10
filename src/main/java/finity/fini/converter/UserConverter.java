package finity.fini.converter;

import finity.fini.domain.User;
import finity.fini.dto.User.UserRequestDTO;
import finity.fini.dto.User.UserResponseDTO;

public class UserConverter {

    public static User toUser(UserRequestDTO.RegisterDTO request, String encodedPassword) {
        return User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .email(request.getEmail())
                .build();
    }

    public static UserResponseDTO.RegisterResultDTO toRegisterResultDTO(User user) {
        return UserResponseDTO.RegisterResultDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .build();
    }

    public static UserResponseDTO.LoginResultDTO toLoginResultDTO(String accessToken) {
        return UserResponseDTO.LoginResultDTO.builder()
                .accessToken(accessToken)
                .build();
    }

    public static UserResponseDTO.MeResultDTO toMeResultDTO(User user) {
        return UserResponseDTO.MeResultDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
