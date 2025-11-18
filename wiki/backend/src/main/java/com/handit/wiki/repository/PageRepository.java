package com.handit.wiki.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.handit.wiki.model.Page;
import com.handit.wiki.model.PageStatus;

public interface PageRepository extends MongoRepository<Page, String> {

    List<Page> findByFolderIdAndDeletedFalse(String folderId);

    List<Page> findByTitleContainingIgnoreCaseAndDeletedFalse(String text);

    List<Page> findByStatusAndDeletedFalse(PageStatus status);

    List<Page> findByDeletedFalse();
}
