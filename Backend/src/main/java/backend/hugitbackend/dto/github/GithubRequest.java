package backend.hugitbackend.dto.github;

import java.time.Instant;
import java.util.UUID;

public record GithubRequest(
        long githubId,
        String githubUserName,
        String displayName,
        String avatarURL,
        String accessToken,
        String tokenScope
) {
}
