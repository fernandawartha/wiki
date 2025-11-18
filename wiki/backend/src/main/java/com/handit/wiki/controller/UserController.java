package com.handit.wiki.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.handit.wiki.dto.UserDto;
import com.handit.wiki.model.Group;
import com.handit.wiki.model.User;
import com.handit.wiki.repository.GroupRepository;
import com.handit.wiki.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final GroupRepository groupRepository;

    public UserController(UserService userService, GroupRepository groupRepository) {
        this.userService = userService;
        this.groupRepository = groupRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        User user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.toDto(user));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(userService.findAll().stream()
                .map(userService::toDto)
                .toList());
    }

    @GetMapping("/groups")
    public ResponseEntity<List<Group>> listGroups() {
        return ResponseEntity.ok(groupRepository.findAll());
    }
}
