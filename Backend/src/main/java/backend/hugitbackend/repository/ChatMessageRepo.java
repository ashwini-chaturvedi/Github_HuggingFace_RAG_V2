package backend.hugitbackend.repository;

import backend.hugitbackend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepo extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage>findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}
