package backend.hugitbackend.service.github;

import backend.hugitbackend.dto.github.GithubRequest;
import backend.hugitbackend.dto.github.GithubResponse;
import backend.hugitbackend.entity.github.Github;
import backend.hugitbackend.repository.github.GithubRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GitHubService {
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
                .accessToken(githubRequest.accessToken())
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
}
