package backend.hugitbackend.dto.huggingface;

import backend.hugitbackend.component.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record HuggingFaceResponse(

        UUID id,
        long huggingFaceId,
        UUID userId,
        String huggingFaceUserName,
        String displayName,
        String avatarUrl,
        String accessToken,
        String refreshToken,
        String tokenScope,
        Instant tokenExpiresAt,
        Instant createdAt,
        Instant updatedAt
) {
}
