package backend.hugitbackend.dto.github;

import backend.hugitbackend.entity.enums.IndexStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;


@Builder
public record GithubRepositoryResponse(
       UUID id,
       long githubId,
       long githubRepoId,
       String owner,
       String name,
       String fullName,
       boolean isPrivate,
       String branch,
       String languages,
       String htmlUrl,
       String description,
       IndexStatus indexStatus,
       Instant indexedAt,
       int chunkCount,
       int filesTotal,
       int filesProcessed,
       String errorMessage,
       Instant createdAt,
       Instant updatedAt
) {

}
