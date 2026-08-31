package backend.devpilotbackend.service.github;

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
public class GithubApiClient {

    private static final String API_BASE="https://api.github.com";
    private static final ParameterizedTypeReference<List<Map<String,Object>>>LIST_MAP=new ParameterizedTypeReference<>() {
    };//to save Repositories
    private static final ParameterizedTypeReference<Map<String,Object>>MAP=new ParameterizedTypeReference<>() {
    };//to save Information regarding the files

    private final RestClient.Builder restClientBuilder;

    private RestClient restClient(String accessToken){
        return restClientBuilder
                .baseUrl(API_BASE)
                .defaultHeader(HttpHeaders.AUTHORIZATION,"Bearer "+accessToken)
                .defaultHeader(HttpHeaders.ACCEPT,"application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version","2022-11-28")
                .defaultHeader(HttpHeaders.USER_AGENT,"DevPilot")
                .build();
    }

    //Retrieves all the repositories of User
    public List<Map<String,Object>>listUserRepos(String accessToken){
        List<Map<String,Object>>allRepos=new ArrayList<>();
        int page=1;
        while(page<=10){
            final int currPage=page;
            List<Map<String,Object>>pageRepos=restClient(accessToken)
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/user/repos")
                            .queryParam("affiliation","owner,collaborator,organization_member")
                            .queryParam("sort","updated")
                            .queryParam("per_page",100)
                            .queryParam("page",currPage)
                            .build()
                    )
                    .retrieve()
                    .body(LIST_MAP);

            if(pageRepos==null || pageRepos.isEmpty()){
                break;
            }
            allRepos.addAll(pageRepos);
            if(pageRepos.size()<100)break;
            page++;

        }

        return allRepos;
    }

    public String getFileContent(String accessToken,String owner,String repoName,String path){
        Map<String,Object>body=restClient(accessToken)
                .get()
                .uri("/repos/{owner}/{repoName}/contents/{path}",owner,repoName,path)
                .retrieve()
                .body(MAP);

        if(body==null) return null;

        Object encoding=body.get("encoding");
        Object content=body.get("content");

        if(content==null) return null;

        if("base64".equals(String.valueOf(encoding))){
            String raw=String.valueOf(content).replaceAll("\\s","");
            return new String(Base64.getDecoder().decode(raw), StandardCharsets.UTF_8);
        }

        return String.valueOf(content);
    }

    public Map<String,Object> getRepoTree(String accessToken,String owner,String repoName,String branch){
        return restClient(accessToken)
                .get()
                .uri("/repos/{owner}/{repo}/git/trees/{branch}?recursive=1",owner,repoName,branch)
                .retrieve()
                .body(MAP);
    }


}
