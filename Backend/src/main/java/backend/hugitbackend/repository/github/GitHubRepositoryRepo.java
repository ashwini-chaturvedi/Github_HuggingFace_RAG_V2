package backend.hugitbackend.repository.github;

import backend.hugitbackend.entity.github.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GitHubRepositoryRepo extends JpaRepository<GithubRepository, UUID> {

    List<GithubRepository> findByGithubIdOrderByFullNameAsc(UUID githubId);

    Optional<GithubRepository>findByIdAndGithubId(UUID id, UUID githubId);
    Optional<GithubRepository>findByGithubIdAndGithubRepoId(UUID githubId, long githubRepoId);
}
