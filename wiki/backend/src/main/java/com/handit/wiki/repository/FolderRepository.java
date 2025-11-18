package com.handit.wiki.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.handit.wiki.model.Folder;

public interface FolderRepository extends MongoRepository<Folder, String> {

    List<Folder> findByParentFolderIdAndDeletedFalseOrderBySortOrderAscNameAsc(String parentFolderId);

    List<Folder> findByDeletedFalse();
}
