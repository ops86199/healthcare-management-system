package com.healthcare.auth.service;

import com.healthcare.auth.dto.LoginRequest;
import com.healthcare.auth.dto.LoginResponse;
import com.healthcare.auth.dto.RegisterRequest;
import com.healthcare.auth.entity.User;
import com.healthcare.auth.exception.InvalidCredentialsException;
import com.healthcare.auth.repository.UserRepository;
import com.healthcare.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidCredentialsException("An account with this email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        String token = jwtService.generateToken(savedUser);

        return buildLoginResponse(savedUser, token);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("Account is deactivated. Please contact support");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        log.info("Login successful for user id: {}", user.getId());

        return buildLoginResponse(user, token);
    }

    private LoginResponse buildLoginResponse(User user, String token) {
        return LoginResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }
}
