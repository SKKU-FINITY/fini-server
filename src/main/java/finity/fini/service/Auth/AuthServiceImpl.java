package finity.fini.service.Auth;

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
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = UserConverter.toUser(request, encodedPassword);

        return userRepository.save(newUser);
    }

    @Override
    public String login(UserRequestDTO.LoginDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        return jwtUtil.createToken(user.getUsername());
    }

    @Override
    public UserResponseDTO.MeResultDTO getMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return UserConverter.toMeResultDTO(user);
    }
}