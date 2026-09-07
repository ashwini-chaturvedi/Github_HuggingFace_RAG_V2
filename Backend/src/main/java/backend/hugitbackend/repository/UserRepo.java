package backend.hugitbackend.repository;

import backend.hugitbackend.entity.User;
import backend.hugitbackend.entity.github.Github;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findByEmailId(String emailId);
    Optional<User> findByGithubId(long githubId);

}
