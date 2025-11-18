package com.handit.wiki.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.handit.wiki.config.SecurityProperties;
import com.handit.wiki.dto.UserDto;
import com.handit.wiki.model.User;
import com.handit.wiki.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            SecurityProperties securityProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    public User save(User user) {
        if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getName(), user.getEmail(), user.getGroupIds());
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        return resolveDefaultUser();
    }

    private User resolveDefaultUser() {
        if (securityProperties.getSuperAdmins() == null) {
            return null;
        }
        for (String identifier : securityProperties.getSuperAdmins()) {
            if (identifier == null || identifier.isBlank()) {
                continue;
            }
            Optional<User> byId = userRepository.findById(identifier);
            if (byId.isPresent()) {
                return byId.get();
            }
            Optional<User> byUsername = userRepository.findByUsernameIgnoreCase(identifier);
            if (byUsername.isPresent()) {
                return byUsername.get();
            }
        }
        return null;
    }
}
