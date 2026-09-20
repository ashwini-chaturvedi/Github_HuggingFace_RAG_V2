package backend.hugitbackend.entity.huggingface;

import backend.hugitbackend.component.EncryptedStringConverter;
import backend.hugitbackend.entity.User;
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
@Table(name = "hugging_face",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id"}))
public class HuggingFace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hugging_face_id",unique = true,nullable = false)
    private String huggingFaceId;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "hugging_face_username",nullable = false,length = 100)
    private String huggingFaceUserName;

    @Column(name = "display_name",nullable = false,length = 200)
    private String displayName;

    @Column(name = "avatarURL",length = 500)
    private String avatarUrl;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "access_token",nullable = false,columnDefinition = "TEXT")
    private String accessToken;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_scopes",length = 500)
    private String tokenScope;


    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate(){
        if(createdAt==null){
            createdAt=Instant.now();
        }
        updatedAt=Instant.now();
    }

    @PreUpdate
    void onUpdate(){
        updatedAt=Instant.now();
    }

}
