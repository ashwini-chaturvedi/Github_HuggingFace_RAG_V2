package backend.devpilotbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Table(name = "chat_sessions")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="user_id",nullable = false)
    private UUID userId;
    @Column(name = "repository_id",nullable = false)
    private UUID repositoryId;
    @Column(nullable = false,length = 200)
    private String title;
    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate(){
        if(createdAt==null)createdAt=Instant.now();
        if(title==null || title.isBlank()) title="New Chat";
    }

}
