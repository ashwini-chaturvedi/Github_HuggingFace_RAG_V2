package backend.hugitbackend.service.indexing;

import backend.hugitbackend.entity.enums.IndexStatus;
import backend.hugitbackend.entity.github.GithubRepository;
import backend.hugitbackend.exceptions.NotFoundException;
import backend.hugitbackend.repository.github.GitHubRepositoryRepo;
import backend.hugitbackend.service.github.GitHubApiClient;
import backend.hugitbackend.service.github.GitHubCodeFileFilter;
import backend.hugitbackend.service.github.GitHubRateLimiter;
import backend.hugitbackend.service.github.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GitHubIndexingAdapter implements RepoIndexingAdapter {

    private final GitHubRepositoryRepo gitHubRepositoryRepo;
    private final GitHubService gitHubService;
    private final GitHubApiClient gitHubApiClient;
    private final GitHubCodeFileFilter gitHubCodeFileFilter;
    private final GitHubRateLimiter gitHubRateLimiter;
    private final CodeChunker codeChunker;

    @Value("${app.indexing.max-file-bytes}")
    private long maxFileBytes;

    @Override
    public UUID connectionIdFor(UUID repoId) {
        return this.gitHubRepositoryRepo.findById(repoId)
                .orElseThrow(()->new NotFoundException("Repository Not Found"))
                .getGithubConnectionId();
    }

    @Override
    public String accessToken(UUID connectionId) {
        return this.gitHubService.requiredById(connectionId).accessToken();
    }

    @Override
    public List<IndexableFile> listFiles(UUID repoId, String accessToken) {
        GithubRepository repository=this.gitHubRepositoryRepo.findById(repoId)
                .orElseThrow(()->new NotFoundException("Repository Not Found"));

        Map<String,Object> tree=this.gitHubApiClient.getRepoTree(accessToken, repository.getOwner(),
                repository.getName(),repository.getBranch());
        if(tree==null || tree.get("tree")==null) return List.of();

        List<Map<String,Object>> entries= (List<Map<String, Object>>) tree.get("tree");

        return entries.stream()
                .filter(e->"blob".equals(String.valueOf(e.get("type"))))
                .map(e->new IndexableFile(
                        String.valueOf(e.get("path")),
                        e.get("size") instanceof Number n? n.longValue():0L))
                .filter(f-> gitHubCodeFileFilter.isEligible(f.path(),
                        f.sizeBytes(),maxFileBytes))
                .toList();
    }

    @Override
    public String fetchContent(String accessToken, UUID repoId, IndexableFile file) {
        this.gitHubRateLimiter.pause();
        GithubRepository repository=this.gitHubRepositoryRepo.findById(repoId)
                .orElseThrow();
        return this.gitHubApiClient.getFileContent(accessToken, repository.getOwner(),
                repository.getOwner(), file.path() );
    }

    @Override
    public List<Document> chunk(UUID repoId, IndexableFile file, String content) {
        String language=this.gitHubCodeFileFilter.detectLanguage(file.path());
        return this.codeChunker.chunkFile(repoId.toString(), file.path(),
                content,language,"GITHUB");
    }

    @Override
    public void updateProgress(UUID repoId, int total, int processed, int chunks, IndexStatus status, String error) {
        this.gitHubRepositoryRepo.findById(repoId).ifPresent(repo->{
            repo.setFilesTotal(total);
            repo.setFilesProcessed(processed);
            repo.setChunkCount(chunks);
            repo.setIndexStatus(status);
            repo.setErrorMessage(error);

            this.gitHubRepositoryRepo.save(repo);
        });
    }

    @Override
    public void markReady(UUID repoId, int totalFiles, int processedFiles, int totalChunks) {
        this.gitHubRepositoryRepo.findById(repoId).ifPresent(repo->{
            repo.setFilesTotal(totalFiles);
            repo.setFilesProcessed(processedFiles);
            repo.setChunkCount(totalChunks);
            repo.setIndexedAt(Instant.now());
            repo.setErrorMessage(null);
            repo.setUpdatedAt(Instant.now());

            this.gitHubRepositoryRepo.save(repo);
        });
    }

    @Override
    public void markFailed(UUID repoId, String message) {
        this.gitHubRepositoryRepo.findById(repoId).ifPresent(repo->{
            repo.setErrorMessage(message);
            repo.setIndexStatus(IndexStatus.FAILED);
            repo.setUpdatedAt(Instant.now());


            this.gitHubRepositoryRepo.save(repo);


        });
    }


}
