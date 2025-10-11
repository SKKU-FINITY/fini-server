package finity.fini.service.Auth;

import finity.fini.apiPayload.code.status.ErrorStatus;
import finity.fini.apiPayload.exception.handler.AuthHandler;
import finity.fini.config.security.jwt.JwtUtil;
import finity.fini.converter.UserConverter;
import finity.fini.domain.User;
import finity.fini.dto.User.UserRequestDTO;
import finity.fini.dto.User.UserResponseDTO;
import finity.fini.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public User register(UserRequestDTO.RegisterDTO request) {
        // 아이디 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthHandler(ErrorStatus.USERNAME_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = UserConverter.toUser(request, encodedPassword);

        return userRepository.save(newUser);
    }

    @Override
    public String login(UserRequestDTO.LoginDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthHandler(ErrorStatus.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthHandler(ErrorStatus.LOGIN_FAILED);
        }

        return jwtUtil.createToken(user.getUsername());
    }

    @Override
    public UserResponseDTO.MeResultDTO getMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthHandler(ErrorStatus.USER_NOT_FOUND));
        return UserConverter.toMeResultDTO(user);
    }
}