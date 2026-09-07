package backend.hugitbackend.dto.huggingface;

import java.time.Instant;
import java.util.UUID;

public record HuggingFaceResponse(
        UUID id,
        long huggingFaceId,
        UUID userId,
        String huggingFaceUserName,
        String displayName,
        String avatarURL,
        String accessToken,
        String tokenScope,
        String refreshToken,
        Instant tokenExpiresAt,
        Instant createdAt
) {
}
