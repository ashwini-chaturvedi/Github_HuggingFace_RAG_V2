package backend.hugitbackend.service.indexing;

import backend.hugitbackend.entity.enums.HuggingFaceRepoType;
import backend.hugitbackend.entity.enums.IndexStatus;
import backend.hugitbackend.entity.huggingface.HuggingFace;
import backend.hugitbackend.entity.huggingface.HuggingFaceRepository;
import backend.hugitbackend.exceptions.NotFoundException;
import backend.hugitbackend.repository.huggingface.HuggingFaceRepositoryRepo;
import backend.hugitbackend.service.huggingface.HuggingFaceApiClient;
import backend.hugitbackend.service.huggingface.HuggingFaceCodeFileFilter;
import backend.hugitbackend.service.huggingface.HuggingFaceRateLimiter;
import backend.hugitbackend.service.huggingface.HuggingFaceService;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HuggingFaceIndexingAdapter implements RepoIndexingAdapter {

    private final HuggingFaceRepositoryRepo huggingFaceRepositoryRepo;
    private final HuggingFaceService huggingFaceService;
    private final HuggingFaceApiClient huggingFaceApiClient;
    private final HuggingFaceCodeFileFilter huggingFaceCodeFileFilter;
    private final CodeChunker codeChunker;
    private final HuggingFaceRateLimiter huggingFaceRateLimiter;
    private final Filter filter;

    @Value("${app.indexing.max-file-bytes}")
    private long maxFileBytes;

    @Override
    public UUID connectionIdFor(UUID repoId) {
        return this.huggingFaceRepositoryRepo.findById(repoId)
                .orElseThrow(()-> new NotFoundException("Can't Find Repository with repoId:"+repoId))
                .getHuggingFaceConnectionId();
    }

    @Override
    public String accessToken(UUID connectionId) {
        return this.huggingFaceService.requireById(connectionId).accessToken();
    }

    @Override
    public List<IndexableFile> listFiles(UUID repoId, String accessToken) {
        HuggingFaceRepository huggingFaceRepository=this.huggingFaceRepositoryRepo
                .findById(repoId)
                .orElseThrow(()-> new NotFoundException("Can't Find the Repository with repoId:"+repoId));

        // HF's tree endpoint returns a FLAT array — no wrapper key like GitHub's "tree",
        // this is the one structurally different bit from GithubIndexingAdapter.
        List<Map<String,Object>> entries=this.huggingFaceApiClient.getRepoTree(
                accessToken,huggingFaceRepository.getHuggingFaceRepoType(),huggingFaceRepository.getOwner(),
                huggingFaceRepository.getName(),huggingFaceRepository.getRevision()
        );

        if(entries==null ) return List.of();

        return entries.stream()
                .filter(e->"file".equals(String.valueOf(e.get("type"))))
                .map(e->new IndexableFile(
                        String.valueOf(e.get("path")),
                        e.get("size")instanceof Number n?n.longValue():0L
                ))
                .filter(f->this.huggingFaceCodeFileFilter.isEligible(
                        f.path(),f.sizeBytes(),maxFileBytes,huggingFaceRepository.getHuggingFaceRepoType()
                ))
                .toList();

    }

    @Override
    public String fetchContent(String accessToken, UUID repoId, IndexableFile file) {
        
    }

    @Override
    public List<Document> chunk(UUID repoId, IndexableFile file, String content) {
        return List.of();
    }

    @Override
    public void updateProgress(UUID repoId, int total, int processed, int chunks, IndexStatus status, String error) {

    }

    @Override
    public void markReady(UUID repoId, int totalFiles, int processedFiles, int totalChunks) {

    }

    @Override
    public void markFailed(UUID repoId, String message) {

    }


}
