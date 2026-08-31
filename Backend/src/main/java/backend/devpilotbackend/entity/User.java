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
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "github_id",unique = true,nullable = false)
    private long githubId;

    @Column(name = "github_username",nullable = false,length = 100)
    private String githubUserName;
    @Column(name = "display_name",nullable = false,length = 200)
    private String displayName;
    @Column(name = "avatarURL",length = 500)
    private String avatarURL;
    @Column(name = "access_token",nullable = false,columnDefinition = "TEXT")
    private String accessToken;
    @Column(name = "token_scopes",length = 500)
    private String tokenScope;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate(){
        if(createdAt==null){
            createdAt=Instant.now();
        }
    }
}
