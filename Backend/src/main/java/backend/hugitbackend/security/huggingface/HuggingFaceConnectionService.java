package backend.hugitbackend.security.huggingface;

import backend.hugitbackend.dto.ProviderTokenData;
import backend.hugitbackend.entity.huggingface.HuggingFace;
import backend.hugitbackend.repository.huggingface.HuggingFaceRepo;
import backend.hugitbackend.security.ProviderConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HuggingFaceConnectionService implements ProviderConnectionService<HuggingFace> {
    private final HuggingFaceRepo huggingFaceRepo;

    @Override
    public Optional<HuggingFace> findByUserId(UUID userId) {
        return this.huggingFaceRepo.findByUserId(userId);
    }

    @Override
    public HuggingFace upsert(UUID userId, ProviderTokenData tokenData) {
        HuggingFace huggingFace=this.huggingFaceRepo.findByUserId(userId)
                .orElseGet(HuggingFace::new);

        huggingFace.setUserId(userId);
        huggingFace.setHuggingFaceId(tokenData.providerUserId());
        huggingFace.setHuggingFaceUserName(tokenData.username());
        huggingFace.setDisplayName(tokenData.displayName());
        huggingFace.setAvatarUrl(tokenData.avatarUrl());
        huggingFace.setAccessToken(tokenData.accessToken());   // encrypted via
        // converter, pe earlier discussion
        huggingFace.setTokenScope(tokenData.scopes());
        huggingFace.setRefreshToken(tokenData.refreshToken());
        huggingFace.setTokenExpiresAt(tokenData.tokenExpiresAt());
        return this.huggingFaceRepo.save(huggingFace);
    }
}
