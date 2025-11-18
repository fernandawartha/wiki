package com.handit.wiki.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.handit.wiki.model.User;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsernameIgnoreCase(String username);
}
