package backend.hugitbackend.dto.huggingface;

import java.time.Instant;
import java.util.UUID;

public record HuggingFaceRequest(
        long huggingFaceId,
        UUID userId,
        String huggingFaceUserName,
        String displayName,
        String avatarUrl,
        String accessToken,
        String refreshToken,
        String tokenScope,
        Instant tokenExpiresAt
) {
}
