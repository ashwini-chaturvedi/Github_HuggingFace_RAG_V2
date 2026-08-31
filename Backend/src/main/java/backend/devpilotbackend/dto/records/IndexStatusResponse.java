package backend.devpilotbackend.dto.records;

import backend.devpilotbackend.entity.IndexStatus;

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
