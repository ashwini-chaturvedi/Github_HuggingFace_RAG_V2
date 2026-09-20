package backend.hugitbackend.service.indexing;

import java.util.List;
import java.util.UUID;

import backend.hugitbackend.entity.enums.IndexStatus;
import org.springframework.ai.document.Document;

public interface RepoIndexingAdapter {
    String accessToken(UUID connectionId);
    List<IndexableFile> listFiles(UUID repoId,String accessToken);
    String fetchContent(String accessToken, UUID repoId,IndexableFile file);
    List<Document>chunk(UUID repoId,IndexableFile file,String content);
    void updateProgress(UUID repoId, int total, int processed, int chunks, IndexStatus status,String error);
    void markReady(UUID repoId,int totalFiles,int processedFiles,int totalChunks);
    void markFailed(UUID repoId,String message);
    UUID connectionIdFor(UUID repoId);

}
