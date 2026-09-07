package backend.hugitbackend.repository;

import backend.hugitbackend.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSessionRepo extends JpaRepository<ChatSession, UUID> {
    List<ChatSession>findByUserIdAndRepositoryIdOrderByCreatedAtDesc(UUID id,UUID repositoryId);
    Optional<ChatSession>findByIdAndUserId(UUID id,UUID userId);
}
