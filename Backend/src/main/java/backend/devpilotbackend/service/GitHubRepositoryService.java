package backend.devpilotbackend.service;

import backend.devpilotbackend.dto.GithubRepositoryResponse;
import backend.devpilotbackend.dto.records.IndexStatusResponse;
import backend.devpilotbackend.dto.UserResponse;
import backend.devpilotbackend.entity.GithubRepository;
import backend.devpilotbackend.exceptions.NotFoundException;
import backend.devpilotbackend.repository.GitHubRepositoryRepo;
import backend.devpilotbackend.service.github.GithubApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GitHubRepositoryService {
    private final GitHubRepositoryRepo gitHubRepoRepository;
    private final UserService userService;
    private final GithubApiClient githubApiClient;

    @Transactional
    public List<GithubRepositoryResponse>syncAndListRepositories(UUID userId){
        UserResponse userResponse=this.userService.requiredById(userId);
        String accessToken=this.userService.decryptAccessToken(userResponse);
        List<Map<String,Object>> githubRepositories=this.githubApiClient.listUserRepos(accessToken);

        List<GithubRepository> saved=new ArrayList<>();

        for(Map<String,Object>repo:githubRepositories){
            Long githubRepoId=toLong(repo.get("id"));
            GithubRepository repository=this.gitHubRepoRepository
                    .findByUserIdAndGithubRepoId(userId,githubRepoId)
                    .orElseGet(GithubRepository::new);

            String fullName=String.valueOf(repo.get("full_name"));
            String[] parts=fullName.split("/",2);

            repository.setUserId(userId);
            repository.setGithubRepoId(githubRepoId);
            repository.setOwner(parts.length>0?parts[0]:String.valueOf(repo.get("owner")));
            repository.setName(parts.length>1?parts[1]:String.valueOf(repo.get("name")));
            repository.setFullName(fullName);
            repository.setPrivate(Boolean.TRUE.equals(repo.get("private")));
            repository.setBranch(repo.get("default_branch")!=null?
                    String.valueOf(repo.get("default_branch")):"main");
            repository.setLanguages(repo.get("language")!=null?
                    String.valueOf(repo.get("language")):null);
            repository.setHtmlUrl(repo.get("html_url")!=null?
                    String.valueOf(repo.get("html_url")):null);
            repository.setDescription(repo.get("description")!=null?
                    String.valueOf(repo.get("description")):null);
            repository.setUpdatedAt(Instant.now());

            if(repository.getOwner()==null || repository.getOwner().isBlank()){
                Object ownerObj=repo.get("owner");
                if(ownerObj instanceof Map<?,?>ownerMap && ownerMap.get("login")!=null){
                    repository.setOwner(String.valueOf(ownerMap.get("login")));
                }
            }

            saved.add(this.gitHubRepoRepository.save(repository));
        }
        return saved.stream()
                .sorted((a,b)->a.getFullName().compareToIgnoreCase(b.getFullName()))
                .map(this::mapResponse)
                .toList();

    }

    @Transactional(readOnly = true)
    public List<GithubRepositoryResponse>sortedList(UUID userId){
        return this.gitHubRepoRepository.findByUserIdOrderByFullNameAsc(userId).stream()
                .map(this::mapResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GithubRepository requiredOwned(UUID repoId,UUID userId){
        return this.gitHubRepoRepository.findByIdAndUserId(repoId,userId)
                .orElseThrow(()->new NotFoundException("Repository Not Found"));
    }

    @Transactional(readOnly = true)
    public IndexStatusResponse status(UUID repoId, UUID userId){
        GithubRepository repository=this.requiredOwned(repoId, userId);
        return new IndexStatusResponse(
                repository.getId(),
                repository.getIndexStatus(),
                repository.getFilesTotal(),
                repository.getFilesProcessed(),
                repository.getChunkCount(),
                repository.getIndexedAt(),
                repository.getErrorMessage()
        );
    }

    //Helper Methods

    private static Long toLong(Object value){
        if(value instanceof Number number){
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }


    public GithubRepositoryResponse mapResponse(GithubRepository repo){
        return GithubRepositoryResponse.builder()
                .id(repo.getId())
                .userId(repo.getUserId())
                .githubRepoId(repo.getGithubRepoId())
                .owner(repo.getOwner())
                .name(repo.getName())
                .fullName(repo.getFullName())
                .isPrivate(repo.isPrivate())
                .branch(repo.getBranch())
                .languages(repo.getLanguages())
                .htmlUrl(repo.getHtmlUrl())
                .description(repo.getDescription())
                .indexStatus(repo.getIndexStatus())
                .indexedAt(repo.getIndexedAt())
                .chunkCount(repo.getChunkCount())
                .filesTotal(repo.getFilesTotal())
                .filesProcessed(repo.getFilesProcessed())
                .errorMessage(repo.getErrorMessage())
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .build();
    }


}
