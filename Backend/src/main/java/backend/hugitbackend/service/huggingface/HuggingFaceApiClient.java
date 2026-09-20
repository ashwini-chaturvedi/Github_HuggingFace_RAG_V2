package backend.hugitbackend.service.huggingface;

import backend.hugitbackend.entity.enums.HuggingFaceRepoType;
import backend.hugitbackend.entity.huggingface.HuggingFace;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HuggingFaceApiClient {

    private static final String API_BASE="https://huggingface.co";
    private static final ParameterizedTypeReference<List<Map<String,Object>>>LIST_MAP=new ParameterizedTypeReference<>() {
    };//to save Repositories
    private static final ParameterizedTypeReference<Map<String,Object>>MAP=new ParameterizedTypeReference<>() {
    };//to save Information regarding the files

    private final RestClient.Builder restClientBuilder;

    private RestClient restClient(String accessToken){
        return restClientBuilder
                .baseUrl(API_BASE)
                .defaultHeader(HttpHeaders.AUTHORIZATION,"Bearer "+accessToken)
                .defaultHeader(HttpHeaders.USER_AGENT,"huGit")
                .build();
    }

    // HF has three parallel endpoint families (models/datasets/spaces) with identical shapes —
    // this is the one thing GitHub's client didn't need, since GitHub only has one repo type.

    private String bestPathFor(HuggingFaceRepoType huggingFaceRepoType){
        return switch (huggingFaceRepoType){
            case MODEL -> "models";
            case SPACE -> "spaces";
            case DATASET -> "datasets";
        };
    }

    //Retrieves all the repositories of User
    public List<Map<String,Object>>listUserRepos(String accessToken,HuggingFaceRepoType hfRepoType){
        return restClient(accessToken)
                .get()
                .uri(uriBuilder -> {
                    var builder=uriBuilder
                            .path("api/settings/repositories")
                            .queryParam("sort","updatedAt")
                            .queryParam("direction","desc")
                            .queryParam("limit",100);

                    if(hfRepoType!=null){
                        String bestPath=this.bestPathFor(hfRepoType);
                        builder.queryParam("type",bestPath.substring(0,bestPath.length()-1));
                        // NB: the query param wants singular "model"/"dataset"/"space", the path wants plural — verify this against a real call, don't trust it blind

                    }
                    return builder.build();
                })
                .retrieve()
                .body(LIST_MAP);
    }

    // Equivalent of getRepoTree — GET /api/{type}/{namespace}/{repo}/tree/{rev}/{path}
    // Returns a flat array of entries directly (unlike GitHub's {"tree": [...]} wrapper),
    // so LIST_MAP works here without any unwrapping.

    public List<Map<String,Object>>getRepoTree(String accessToken,HuggingFaceRepoType hfRepoType,
                                               String nameSpace,String repo,String revision){
        String base=bestPathFor(hfRepoType);

        return restClient(accessToken)
                .get()
                .uri("api/{base}/{nameSpace}/{repo}/tree/{revision}/?recursive=true",base,nameSpace,repo,revision)
                .retrieve()
                .body(LIST_MAP);
    }

    // Equivalent of getFileContent — but the shape is genuinely different from GitHub's.
    // GitHub embeds base64 content inline in a JSON response.
    // HF's resolve endpoint is a straight file download behind a redirect — no JSON wrapper,
    // no encoding field to check. RestClient's default HTTP client follows redirects automatically.

    public String getFileContent(String accessToken,HuggingFaceRepoType hfRepoType,
                                 String namespace,String repo,String revision,String path){
        String base=bestPathFor(hfRepoType);
        byte[] raw=restClient(accessToken)
                .get()
                .uri("/{namespace}/{repo}/resolve/{revision}/{path}",namespace,repo,revision,path)
                .retrieve()
                .body(byte[].class);

        if(raw==null) return null;

        return new String(raw,StandardCharsets.UTF_8);
            // WARNING: only call this for text files (README.md, config.json, source in Space repos).
            // If this hits a .safetensors/.bin/.gguf file, you'll get garbage — your GitHubCodeFileFilter
            // needs to reject binary extensions BEFORE calling this, same discipline as your
            // GitHubApiClient already assumes about GitHub's binary files, just enforced earlier here
            // since there's no encoding-field escape hatch to check first.
    }

    // Equivalent of "get repo info" — no GitHubApiClient counterpart shown, but you'll want it.
    // Flagged earlier as unverified against this specific OpenAPI doc — test with a real call first.
    public Map<String,Object>getRepoMetaData(String accessToken, HuggingFaceRepoType hfRepoType,
                                             String namespace,String repo){
        String base=bestPathFor(hfRepoType);
        return restClient(accessToken)
                .get()
                .uri("/api/{base}/{namespace}/{repo}",base,namespace,repo)
                .retrieve()
                .body(MAP);
    }
    // No GitHub equivalent needed (GitHub tokens don't need a liveness check the same way) —
    // but worth having, since you'll use it right after storing a token to confirm it's valid
    // and to backfill HuggingFace.huggingFaceUserName/displayName at connect time.

    public Map<String,Object> who_am_i(String accessToken){
        return restClient(accessToken)
                .get()
                .uri("/api/whoami-v2")
                .retrieve()
                .body(MAP);
    }




}
