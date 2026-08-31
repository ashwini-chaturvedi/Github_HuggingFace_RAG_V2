package backend.devpilotbackend.repository;

import backend.devpilotbackend.entity.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GitHubRepositoryRepo extends JpaRepository<GithubRepository, UUID> {

    List<GithubRepository>findByUserIdOrderByFullNameAsc(UUID userId);

    Optional<GithubRepository>findByIdAndUserId(UUID id,UUID userId);
    Optional<GithubRepository>findByUserIdAndGithubRepoId(UUID userId, Long githubRepoId);
}
