package com.handit.wiki.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.handit.wiki.model.PageHistory;

public interface PageHistoryRepository extends MongoRepository<PageHistory, String> {

    List<PageHistory> findByPageIdOrderByVersionNumberDesc(String pageId);
}
