package backend.devpilotbackend.service.ai;

import org.springframework.stereotype.Component;

@Component
public final class RAG_Settings {

    //How many code chunks to fetch from vectorDB perQuestion
    public static final int TOP_K_CHUNKS=8;
    //Max Time to keep an SSE Stream open while the model is Responding
    public static final long STREAM_TIMEOUT_MS=180_000L;
    //Metadata key stored on each Embedded document
    public static final String METADATA_REPO_ID="repoId";

    private RAG_Settings(){

    }
}
