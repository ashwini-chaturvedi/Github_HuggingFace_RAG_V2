package backend.hugitbackend.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;


import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeContextRetriever {
    //This will help to attach the Context based on the Question asked
    private static final String NO_MATCHES="(no matching code chunk Found)";

    private final VectorStore vectorStore;
    private final CitationMapper citationMapper;

    public RetrievedContext retrievedContext(UUID repoId,String question){
        var filter=new FilterExpressionBuilder()
                .eq(RAG_Settings.METADATA_REPO_ID,repoId.toString())
                .build();

        var search= SearchRequest.builder()
                .query(question)
                .topK(RAG_Settings.TOP_K_CHUNKS)//Algorithm for ANN Searching
                .filterExpression(filter)
                .build();

        var documents=this.vectorStore.similaritySearch(search);

        var citations=documents.stream()
                .map(citationMapper::fromDocument)
                .distinct()
                .toList();

        var contextText=documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        if(contextText.isBlank()) contextText=NO_MATCHES;

        return new RetrievedContext(citations,contextText);
    }
}
