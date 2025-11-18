package com.handit.wiki.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.handit.wiki.model.Group;

public interface GroupRepository extends MongoRepository<Group, String> {
}
