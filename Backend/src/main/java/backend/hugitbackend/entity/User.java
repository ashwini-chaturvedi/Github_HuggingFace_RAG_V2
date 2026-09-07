package backend.hugitbackend.entity;

import backend.hugitbackend.entity.github.Github;
import backend.hugitbackend.entity.huggingface.HuggingFace;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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

    @Email
    @Column(name = "email_id")
    private String emailId;

    @Column(name = "password")
    private String password;

    @Column(name = "user_name",nullable = false)
    private String userName;

    @Column(length = 500)
    private String description;

    @Column(name = "github_id")
    private long githubId;

    @Column(name = "hugging_face_id")
    private long huggingFaceId;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate(){
        if(createdAt==null){
            createdAt=Instant.now();
        }
    }
}
