package backend.hugitbackend.repository.github;

import backend.hugitbackend.entity.github.Github;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GithubRepo extends JpaRepository<Github, UUID> {
    @NonNull Optional<Github>findById(@NonNull UUID id);
   Optional<Github>findByGithubId( long githubId);
   Optional<Github>findByUserId( UUID userId);
}
