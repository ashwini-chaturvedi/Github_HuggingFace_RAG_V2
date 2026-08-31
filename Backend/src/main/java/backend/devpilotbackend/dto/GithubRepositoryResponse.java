package backend.devpilotbackend.dto;

import backend.devpilotbackend.entity.IndexStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class GithubRepositoryResponse {
    private UUID id;
    private UUID userId;
    private Long githubRepoId;
    private String owner;
    private  String name;
    private String fullName;
    @JsonProperty("isPrivate")
    private boolean isPrivate;
    private String branch;
    private String languages;
    private String htmlUrl;
    private String description;
    private IndexStatus indexStatus=IndexStatus.PENDING;
    private Instant indexedAt;
    private int chunkCount=0;
    private int filesTotal=0;
    private int filesProcessed=0;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;


}
