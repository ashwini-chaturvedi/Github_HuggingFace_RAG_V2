package backend.devpilotbackend.service.indexing;

import backend.devpilotbackend.entity.GithubRepository;
import backend.devpilotbackend.entity.IndexStatus;
import backend.devpilotbackend.exceptions.BadRequestException;
import backend.devpilotbackend.exceptions.NotFoundException;
import backend.devpilotbackend.repository.GitHubRepositoryRepo;
import backend.devpilotbackend.service.UserService;
import backend.devpilotbackend.service.ai.RAG_Settings;
import backend.devpilotbackend.service.github.GithubApiClient;
import backend.devpilotbackend.service.github.GithubRateLimiter;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingService {

    private static final int VECTOR_BATCH_SIZE=32;
    private static final int PROGRESS_EVERY_N_FILES=5;

    private final GitHubRepositoryRepo gitHubRepositoryRepo;
    private final UserService userService;
    private final GithubApiClient githubApiClient;
    private final CodeFileFilter codeFileFilter;
    private final CodeChunker codeChunker;
    private final GithubRateLimiter githubRateLimiter;
    private final VectorStore vectorStore;

    @Value("${app.indexing.max-file-bytes}")
    private long maxFileBytes;


    public GithubRepository startIndexing(UUID repoId,UUID userId){
        GithubRepository repository=this.gitHubRepositoryRepo.findByIdAndUserId(repoId,userId)
                .orElseThrow(()->new NotFoundException("Repository Not Found"));

        if(repository.getIndexStatus()== IndexStatus.INDEXING){
            throw new BadRequestException("Repository is already being Indexed");
        }

        repository.setIndexStatus(IndexStatus.INDEXING);
        repository.setChunkCount(0);
        repository.setFilesTotal(0);
        repository.setFilesProcessed(0);
        repository.setErrorMessage(null);
        repository.setUpdatedAt(Instant.now());

        return this.gitHubRepositoryRepo.save(repository);
    }

    @Async(("indexingExecutor"))
    public void indexAsync(UUID repoId,UUID userId){
        try{
            startIndexingRepository(repoId,userId);
        }catch (Exception e){
            log.error("Indexing failed for repo {}",repoId,e);
            markFailed(repoId,e.getMessage());
        }
    }

    private void startIndexingRepository(UUID repoId,UUID userId){
        //Get the Repository
        GithubRepository repo=this.gitHubRepositoryRepo.findById(repoId)
                .orElseThrow(()->new NotFoundException("Repository Not Found!!!"));
        //get the accesstoke
        String token=this.userService.decryptAccessToken(this.userService.requiredById(userId));

        deleteExistingVectors(repoId);//Delete any preexisting vector for this repository

        //get the Repositories Tree.
        Map<String,Object> tree=this.githubApiClient.getRepoTree(token,repo.getOwner(),repo.getName(),
                repo.getBranch());
        List<String> filePaths=this.listIndexableFiles(tree);//files

        //Now we will start indexing so update the status and Start Indexing
        updateProgress(repoId,filePaths.size(),0,0,IndexStatus.INDEXING,null);

        //Start Processing batch by batch
        List<Document>batch=new ArrayList<>();
        int processed=0;
        int totalChunks=0;

        for(String path:filePaths){
            try {
                String content=this.githubApiClient.getFileContent(token, repo.getOwner(),
                        repo.getName(),path);
                List<Document>chunks=this.codeChunker.chunkFile(repoId.toString(),path,content);
                batch.addAll(chunks);
                totalChunks+=chunks.size();

                if(batch.size()>=VECTOR_BATCH_SIZE){
                    this.vectorStore.add(batch);//adding to the Vector DB
                    batch.clear();
                }
            }catch (Exception e){
                log.warn("Skipping file {} in {}: {}",path,repo.getFullName(),e.getMessage());

            }
            processed++;

            if(processed%PROGRESS_EVERY_N_FILES==0 || processed==filePaths.size()){
                updateProgress(repoId,filePaths.size(),processed,totalChunks,IndexStatus.INDEXING,null);
            }
            githubRateLimiter.pause();
        }
        if(!batch.isEmpty()) vectorStore.add(batch);

        markReady(repoId,filePaths.size(),processed,totalChunks,repo.getFullName());
    }

    private List<String> listIndexableFiles(Map<String,Object>tree){
        if(tree==null || tree.get("tree")==null ) return List.of();

        List<Map<String,Object>>entries=(List<Map<String,Object>>)tree.get("tree");

        return entries.stream()
                .filter(entry->"blob".equals(String.valueOf(entry.get("type"))))
                .filter(entry->{
                    String path=String.valueOf(entry.get("path"));
                    long size=entry.get("size") instanceof Number n?n.longValue():0L;
                    return this.codeFileFilter.isEligible(path,size,maxFileBytes);
                })
                .map(entry->String.valueOf(entry.get("path")))
                .toList();
    }

    public void deleteExistingVectors(UUID repoId){
        try {
            var filter=new FilterExpressionBuilder().eq(RAG_Settings.METADATA_REPO_ID,repoId).build();
            this.vectorStore.delete(filter);
        }catch (Exception e){
            log.warn("Could not Delete existing vectors for repo {}:{}",repoId,e.getMessage());
        }
    }

    @Transactional
    protected void updateProgress(UUID repoId,int total,int processed,int chunks,IndexStatus status,String error){
        this.gitHubRepositoryRepo.findById(repoId).ifPresent(repo->{
            repo.setFilesTotal(total);
            repo.setFilesProcessed(processed);
            repo.setChunkCount(chunks);
            repo.setIndexStatus(status);
            repo.setErrorMessage(error);

            this.gitHubRepositoryRepo.save(repo);
        });
    }

    @Transactional
    public void markReady(UUID repoId,int totalFiles,int processedFiles,int totalChunks,String fullName){
        this.gitHubRepositoryRepo.findById(repoId).ifPresent(repo->{
            repo.setIndexStatus(IndexStatus.READY);
            repo.setFilesTotal(totalFiles);
            repo.setFilesProcessed(processedFiles);
            repo.setChunkCount(totalChunks);
            repo.setIndexedAt(Instant.now());
            repo.setErrorMessage(null);
            repo.setUpdatedAt(Instant.now());

            this.gitHubRepositoryRepo.save(repo);
        });
        log.info("Indexed {} files ({} chunks) for {}",processedFiles,totalChunks,fullName);
    }
    @Transactional
    public void markFailed(UUID repoId,String msg){
        this.gitHubRepositoryRepo.findById(repoId).ifPresent(repo->{
            repo.setIndexStatus(IndexStatus.FAILED);
            repo.setErrorMessage(msg!=null && msg.length()>2000?
                    msg.substring(0,2000):
                    msg
                    );
            repo.setUpdatedAt(Instant.now());

            this.gitHubRepositoryRepo.save(repo);
        });
    }

}
