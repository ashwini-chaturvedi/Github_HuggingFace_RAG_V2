package backend.hugitbackend.service.github;

import backend.hugitbackend.dto.github.GithubRequest;
import backend.hugitbackend.dto.github.GithubResponse;
import backend.hugitbackend.entity.github.Github;
import backend.hugitbackend.repository.github.GithubRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GithubService {
    private final TextEncryptor tokenEncryptor;
    private final GithubRepo githubRepo;

    public GithubResponse saveGithub(GithubRequest githubRequest){
        Github github=this.mapGithubRequestToGithub(githubRequest);
        github=this.githubRepo.save(github);

        return this.mapGithubToGithubResponse(github);
    }

    @Transactional(readOnly = true)
    public GithubResponse requiredById(UUID id){
        Github github=this.githubRepo.findById(id)
                .orElseThrow(()->new IllegalArgumentException("Can't find Github with id:"+id));

        return this.mapGithubToGithubResponse(github);
    }

    @Transactional
    public Github upsertFromGitHub(Map<String, Object> attributes, String accessToken, String scopes) {
        Long githubId=toLong(attributes.get("id"));
        String login=String.valueOf(attributes.get("login"));
        String name=attributes.get("name")!=null
                ?String.valueOf(attributes.get("name"))
                :login;
        String avatarUrl=attributes.get("avatar_url")!=null?
                String.valueOf(attributes.get("avatar_url"))
                :null;
        String encryptToken=tokenEncryptor.encrypt(accessToken);

        Github github= this.githubRepo.findByGithubId(githubId).orElseGet(Github::new);

        github.setGithubId(githubId);
        github.setGithubUserName(login);
        github.setAvatarURL(avatarUrl);
        github.setAccessToken(encryptToken);
        github.setDisplayName(name);
        github.setTokenScope(scopes);

        user.setGithub(github);


        return  this.userRepository.save(user);
    }

    //Helper methods
    private static Long toLong(Object value){
        if(value instanceof Number number){
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
    private Github mapGithubRequestToGithub(GithubRequest githubRequest){
        return Github.builder()
                .githubId(githubRequest.githubId())
                .githubUserName(githubRequest.githubUserName())
                .displayName(githubRequest.displayName())
                .avatarURL(githubRequest.avatarURL())
                .accessToken(this.tokenEncryptor.encrypt(githubRequest.accessToken()))
                .tokenScope(githubRequest.tokenScope())
                .build();
    }
    private GithubResponse mapGithubToGithubResponse(Github github){
        return GithubResponse.builder()
                .id(github.getId())
                .githubId(github.getGithubId())
                .githubUserName(github.getGithubUserName())
                .displayName(github.getDisplayName())
                .avatarURL(github.getAvatarURL())
                .accessToken(github.getAccessToken())
                .tokenScope(github.getTokenScope())
                .createdAt(github.getCreatedAt())
                .build();
    }

    public String decryptAccessToken(GithubResponse githubResponse){
        return this.tokenEncryptor.decrypt(githubResponse.accessToken());
    }
}
