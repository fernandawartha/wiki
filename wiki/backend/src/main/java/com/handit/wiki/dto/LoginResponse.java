package com.handit.wiki.dto;

public record LoginResponse(String token, UserDto user) {
}
