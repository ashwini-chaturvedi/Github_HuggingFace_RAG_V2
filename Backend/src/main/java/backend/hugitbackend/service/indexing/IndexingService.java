package backend.hugitbackend.service.indexing;

import backend.hugitbackend.entity.github.GithubRepository;
import backend.hugitbackend.entity.enums.IndexStatus;
import backend.hugitbackend.exceptions.BadRequestException;
import backend.hugitbackend.exceptions.NotFoundException;
import backend.hugitbackend.service.ai.RAG_Settings;
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

    private final VectorStore vectorStore;

    @Value("${app.indexing.max-file-bytes}")
    private long maxFileBytes;


    @Async(("indexingExecutor"))
    public void indexAsync(UUID repoId,RepoIndexingAdapter repoIndexingAdapter){
        try{
            run(repoId,repoIndexingAdapter);
        }catch (Exception e){
            log.error("Indexing failed for repo {}",repoId,e);
            repoIndexingAdapter.markFailed(repoId,e.getMessage());
        }
    }

    private void run(UUID repoId,RepoIndexingAdapter repoIndexingAdapter){
        UUID connectionId=repoIndexingAdapter.connectionIdFor(repoId);
        String token=repoIndexingAdapter.accessToken(connectionId);

        deleteExistingVectors(repoId);

        List<IndexableFile> files=repoIndexingAdapter.listFiles(repoId,token);
        repoIndexingAdapter.updateProgress(repoId,files.size(),0,0,IndexStatus.INDEXING,null);

        List<Document> batch=new ArrayList<>();
        int processed=0;
        int totalChunks=0;

        for(IndexableFile file:files){
            try {
                String content=repoIndexingAdapter.fetchContent(token,repoId,file);
                List<Document> chunks=repoIndexingAdapter.chunk(repoId,file,content);
                batch.addAll(chunks);
                totalChunks+=chunks.size();

                if(batch.size()>=VECTOR_BATCH_SIZE){
                    this.vectorStore.add(batch);
                    batch.clear();
                }
            } catch (Exception e) {
                log.warn("Skipping file {} in repo {}: {}", file.path(), repoId, e.getMessage());

            }

            processed++;

            if(processed%PROGRESS_EVERY_N_FILES==0|| processed==files.size()){
                repoIndexingAdapter.updateProgress(repoId, files.size(), processed,totalChunks,IndexStatus.INDEXING,
                        null);
            }
        }

        if(!batch.isEmpty())this.vectorStore.add(batch);

        repoIndexingAdapter.markReady(repoId,files.size(),processed,totalChunks);
    }

    private void deleteExistingVectors(UUID repoId){
        try {
            var filter=new FilterExpressionBuilder().eq(RAG_Settings.METADATA_REPO_ID,repoId).build();

            this.vectorStore.delete(filter);
        } catch (Exception e) {
            log.warn("Could not delete existing vectors for repo {}: {}", repoId, e.getMessage());
        }
    }


}
