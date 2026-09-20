package backend.hugitbackend.service.indexing;

import backend.hugitbackend.service.ai.RAG_Settings;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class CodeChunker {
    private final TokenTextSplitter tokenTextSplitter;

    public CodeChunker(@Value("${app.indexing.chunk-size}")int chunkSize){
        //Spring-AI splits by tokes:~4 characters per token is a Reasonable default for code.
        int chunkTokens=Math.max(50,chunkSize/4);
        this.tokenTextSplitter= TokenTextSplitter.builder()
                .withChunkSize(chunkTokens)
                .build();
    }

    public List<Document>chunkFile(String repoId,String filePath,String content,
                                   String language,String sourceType){
        if(content==null || content.isBlank()) return List.of();


        String header="// File: "+filePath+"\n";

        Document sourceDocument=new Document(header+content,baseMetaData(repoId,filePath,language,sourceType));
        List<Document>split=this.tokenTextSplitter.apply(List.of(sourceDocument));

        return IntStream.range(0,split.size())
                .mapToObj(i->withChunkIndex(split.get(i),repoId,filePath,language,sourceType,i))
                .toList();
    }


    //Helpers
    private static Document withChunkIndex(Document chunk,String repoId,String filePath,String language,
                                           String sourceType,int chunkIndex){
        Map<String ,Object> metaData=new HashMap<>(chunk.getMetadata());
        metaData.put(RAG_Settings.METADATA_REPO_ID,repoId);
        metaData.put("filePath",filePath);
        metaData.put("language",language);
        metaData.put("chunkIndex",chunkIndex);
        metaData.put("sourceType",sourceType);// NEW — "GITHUB" | "HF_MODEL" | "HF_DATASET" | "HF_SPACE"
        return new Document(chunk.getText(),metaData);
    }


    private static Map<String,Object>baseMetaData(String repoId, String filePath, String language, String sourceType){
        Map<String,Object>metaData=new HashMap<>();
        metaData.put(RAG_Settings.METADATA_REPO_ID,repoId);
        metaData.put("filePath",filePath);
        metaData.put("language",language);
        metaData.put("sourceType",sourceType);// NEW — "GITHUB" | "HF_MODEL" | "HF_DATASET" | "HF_SPACE"
        return metaData;
    }

}
