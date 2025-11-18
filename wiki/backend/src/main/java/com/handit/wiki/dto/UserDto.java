package com.handit.wiki.dto;

import java.util.List;

public record UserDto(
        String id,
        String username,
        String name,
        String email,
        List<String> groups) {
}
