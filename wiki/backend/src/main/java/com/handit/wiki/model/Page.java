package com.handit.wiki.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "pages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Page {

    @Id
    private String id;
    private String title;
    private String folderId;
    private PageStatus status;
    private String bitbucketPath;
    private String summary;
    private boolean deleted;

    private String createdBy;
    private String updatedBy;

    private List<String> permissionIds;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
