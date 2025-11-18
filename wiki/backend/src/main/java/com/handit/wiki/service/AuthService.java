package com.handit.wiki.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.handit.wiki.dto.LoginRequest;
import com.handit.wiki.dto.LoginResponse;
import com.handit.wiki.dto.RegisterRequest;
import com.handit.wiki.dto.UserDto;
import com.handit.wiki.model.User;
import com.handit.wiki.security.JwtService;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userService.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, userService.toDto(user));
    }

    public UserDto register(RegisterRequest request) {
        User user = User.builder()
                .username(request.username())
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        return userService.toDto(userService.save(user));
    }
}
