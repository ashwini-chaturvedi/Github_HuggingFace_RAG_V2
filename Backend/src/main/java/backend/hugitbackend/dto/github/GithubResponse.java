package backend.hugitbackend.dto.github;


import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record GithubResponse(
        UUID id,
        long githubId,
        String githubUserName,
        String displayName,
        String avatarURL,
        String accessToken,
        String tokenScope,
        Instant createdAt) {
}
