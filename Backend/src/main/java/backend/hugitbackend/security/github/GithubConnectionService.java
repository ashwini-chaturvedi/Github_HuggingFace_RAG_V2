package backend.hugitbackend.security.github;

import backend.hugitbackend.dto.ProviderTokenData;
import backend.hugitbackend.entity.github.Github;
import backend.hugitbackend.repository.github.GithubRepo;
import backend.hugitbackend.security.ProviderConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GithubConnectionService implements ProviderConnectionService<Github> {
    private final GithubRepo githubRepo;
    @Override
    public Optional<Github> findByUserId(UUID userId) {
        return this.githubRepo.findByUserId(userId);
    }

    @Override
    public Github upsert(UUID userId, ProviderTokenData tokenData) {
        Github github=this.githubRepo.findByUserId(userId).orElseGet(Github::new);
        github.setUserId(userId);
        github.setGithubId(Long.parseLong(tokenData.providerUserId()));
        github.setGithubUserName(tokenData.username());
        github.setDisplayName(tokenData.displayName());
        github.setAvatarURL(tokenData.avatarUrl());
        github.setAccessToken(tokenData.accessToken());   // encrypted via converter, per earlier
        // discussion
        github.setTokenScope(tokenData.scopes());
        return this.githubRepo.save(github);
    }
}
