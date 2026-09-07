package backend.hugitbackend.entity.github;

import backend.hugitbackend.entity.enums.IndexStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Builder
@Entity
@Table(name = "github_repositories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","github_repo_id"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GithubRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "github_id", nullable = false)
    private UUID githubId;

    @Column(name = "github_repo_id",nullable = false)
    private long githubRepoId;

    @Column(length = 100,nullable = false)
    private String owner;
    @Column(length = 200,nullable = false)
    private  String name;
    @Column( length = 300,nullable = false)
    private String fullName;

    @Column(name = "is_private_repo",nullable = false)
    private boolean isPrivate;

    @Column(name = "default_branch",nullable = false,length = 100)
    private String branch;

    @Column(length = 100)
    private String languages;

    @Column(name = "html_url",length = 500)
    private String htmlUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Vector Embeddings-RAG system variables

    @Enumerated(EnumType.STRING)
    @Column(name = "index_status",nullable = false,length = 20)
    @Builder.Default
    private IndexStatus indexStatus=IndexStatus.PENDING;//when we give a data to the AI for RAG systems we Index
    // those data to keep track of it during vector Embeddings.

    @Column(name="indexed_at")
    private Instant indexedAt;

    @Column(name="chunk_count",nullable = false)
    @Builder.Default
    private int chunkCount=0;

    @Column(name="files_total",nullable = false)
    @Builder.Default
    private int filesTotal=0;
    @Column(name="files_processed",nullable = false)
    @Builder.Default
    private int filesProcessed=0;

    @Column(name="error_message",columnDefinition = "TEXT")
    private String errorMessage;
    @Column(name="created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate(){
        Instant now=Instant.now();
        if(createdAt==null) createdAt=now;
        updatedAt=now;
        if(indexStatus==null)indexStatus=IndexStatus.PENDING;
    }


}
