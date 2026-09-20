package backend.hugitbackend.security;


import backend.hugitbackend.dto.ProviderTokenData;

import java.util.Optional;
import java.util.UUID;

public interface ProviderConnectionService <C>{
    Optional<C>findByUserId(UUID userId);
    C upsert(UUID userId, ProviderTokenData tokenData);
}
