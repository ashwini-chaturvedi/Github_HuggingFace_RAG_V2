package backend.hugitbackend.dto;

import java.time.Instant;

public record ProviderTokenData(
        String providerUserId,
        String username,
        String displayName,
        String avatarUrl,
        String accessToken,
        String refreshToken,      // null for GitHub
        Instant tokenExpiresAt,   // null for GitHub
        String scopes
) {
}
