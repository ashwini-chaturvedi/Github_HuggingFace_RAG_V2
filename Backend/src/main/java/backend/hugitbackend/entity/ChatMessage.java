package backend.hugitbackend.entity;

import backend.hugitbackend.entity.enums.MessageRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id",nullable = false)
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private MessageRole role;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String citations;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate(){
        if(createdAt==null) createdAt=Instant.now();

    }

}
