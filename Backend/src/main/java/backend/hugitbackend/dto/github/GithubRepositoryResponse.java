package backend.hugitbackend.dto.github;

import backend.hugitbackend.entity.enums.IndexStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;


@Builder
public record GithubRepositoryResponse(
       UUID id,
       UUID githubConnectionId,
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
