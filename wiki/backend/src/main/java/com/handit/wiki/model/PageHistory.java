package com.handit.wiki.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "pageHistory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageHistory {

    @Id
    private String id;
    private String pageId;
    private int versionNumber;
    private String authorId;
    private String bitbucketCommitHash;
    private String notes;

    @CreatedDate
    private Instant timestamp;
}
