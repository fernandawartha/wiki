package com.handit.wiki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.handit.wiki.model.Permission;
import com.handit.wiki.model.PermissionSubjectType;
import com.handit.wiki.model.PermissionTargetType;

public interface PermissionRepository extends MongoRepository<Permission, String> {

    List<Permission> findByTargetTypeAndTargetId(PermissionTargetType type, String targetId);

    Optional<Permission> findByTargetTypeAndTargetIdAndSubjectTypeAndSubjectId(
            PermissionTargetType type,
            String targetId,
            PermissionSubjectType subjectType,
            String subjectId);
}
