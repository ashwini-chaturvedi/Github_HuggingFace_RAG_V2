package backend.hugitbackend.dto.huggingface;

import backend.hugitbackend.entity.enums.HuggingFaceRepoType;
import backend.hugitbackend.entity.enums.IndexStatus;

import java.time.Instant;
import java.util.UUID;

public record HuggingFaceRepositoryRequest(
        long huggingFaceId,
        String repoId,
        HuggingFaceRepoType huggingFaceRepoType,
        String owner,
        String name,
        boolean isPrivate,
        boolean isGated,
        String revision,
        String pipelineTag,
        String libraryName,
        String htmlUrl,
        String description,
        IndexStatus indexStatus,
        int chunkCount,
        int filesTotal,
        int filesProcessed,
        String errorMessage,
        Instant indexedAt
) {
}
