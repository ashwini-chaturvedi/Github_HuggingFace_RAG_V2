package backend.hugitbackend.dto.records;

import backend.hugitbackend.entity.enums.IndexStatus;

import java.time.Instant;
import java.util.UUID;

public record IndexStatusResponse (
    UUID repositoryId,
    IndexStatus indexStatus,
    int filesTotal,
    int filesProccessed,
    int chunkCount,
    Instant indexedAt,
    String errorMessage){

}
