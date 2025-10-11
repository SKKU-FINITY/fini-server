package finity.fini.service.Auth;

import finity.fini.domain.User;
import finity.fini.dto.User.UserRequestDTO;
import finity.fini.dto.User.UserResponseDTO;

public interface AuthService {
    User register(UserRequestDTO.RegisterDTO request);
    String login(UserRequestDTO.LoginDTO request);
    UserResponseDTO.MeResultDTO getMe(String username);
}
