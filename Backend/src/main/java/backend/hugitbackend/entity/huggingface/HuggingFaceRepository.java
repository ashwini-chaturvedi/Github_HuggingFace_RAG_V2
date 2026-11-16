package backend.hugitbackend.entity.huggingface;

import backend.hugitbackend.entity.enums.HuggingFaceRepoType;
import backend.hugitbackend.entity.enums.IndexStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "github_repositories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hugging_face_connection_id","repo_id"}))
public class HuggingFaceRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hugging_face_connection_id",nullable = false)
    private UUID huggingFaceConnectionId;

    @Column(name = "repo_id",nullable = false,length = 300)
    private String repoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "repo_type",nullable = false,length = 20)
    private HuggingFaceRepoType huggingFaceRepoType;

    @Column(length = 100,nullable = false)
    private String owner;

    @Column(length = 200,nullable = false)
    private String name;

    @Column(name="is_private",nullable = false)
    private boolean isPrivate;

    @Column(name = "is_gated",nullable = false)
    private boolean isGated;

    @Column(name = "revision",nullable = false,length = 100)
    private String revision;

    @Column(name = "pipeline_tag",length = 100)
    private String pipelineTag;

    @Column(name = "library_name",length = 100)
    private String libraryName;

    @Column(name = "html_url",length = 500)
    private String htmlUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "index_status",nullable = false,length = 20)
    @Builder.Default
    private IndexStatus indexStatus= IndexStatus.PENDING;

    @Column(name = "chunk_count",nullable = false)
    @Builder.Default
    private int chunkCount=0;

    @Column(name = "files_total",nullable = false)
    @Builder.Default
    private int filesTotal=0;

    @Column(name = "files_processed",nullable = false)
    @Builder.Default
    private int filesProcessed=0;

    @Column(name = "error_message",columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "indexed_at")
    private Instant indexedAt;

    @Column(name= "created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate(){
        Instant now=Instant.now();
        if(createdAt==null) createdAt=now;
        updatedAt=now;
        if(revision==null) revision="main";
    }

    @PostUpdate
    void onUpdate(){
        updatedAt=Instant.now();
    }


}
