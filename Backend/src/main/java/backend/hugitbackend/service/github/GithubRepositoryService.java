package backend.hugitbackend.service.github;

import backend.hugitbackend.dto.github.GithubRepositoryResponse;
import backend.hugitbackend.dto.github.GithubResponse;
import backend.hugitbackend.dto.records.IndexStatusResponse;
import backend.hugitbackend.entity.enums.IndexStatus;
import backend.hugitbackend.entity.github.GithubRepository;
import backend.hugitbackend.exceptions.NotFoundException;
import backend.hugitbackend.repository.github.GitHubRepositoryRepo;
import backend.hugitbackend.service.UserService;
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
public class GithubRepositoryService {
    private final GitHubRepositoryRepo gitHubRepoRepository;
    private final UserService userService;
    private final GithubService githubService;
    private final GithubApiClient githubApiClient;

    @Transactional
    public List<GithubRepositoryResponse>syncAndListRepositories(UUID githubId){
        GithubResponse githubResponse=this.githubService.requiredById(githubId);
        String accessToken=this.githubService.decryptAccessToken(githubResponse);
        List<Map<String,Object>> githubRepositories=this.githubApiClient.listUserRepos(accessToken);

        List<GithubRepository> saved=new ArrayList<>();

        for(Map<String,Object>repo:githubRepositories){
            Long githubRepoId=toLong(repo.get("id"));
            GithubRepository repository=this.gitHubRepoRepository
                    .findByGithubIdAndGithubRepoId(githubId,githubRepoId)
                    .orElseGet(GithubRepository::new);

            String fullName=String.valueOf(repo.get("full_name"));
            String[] parts=fullName.split("/",2);

            repository.setGithubId(githubId);
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
    public List<GithubRepositoryResponse>sortedList(UUID githubId){
        return this.gitHubRepoRepository.findByGithubIdOrderByFullNameAsc(githubId).stream()
                .map(this::mapResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GithubRepository requiredOwned(UUID repoId, UUID githubId){
        return this.gitHubRepoRepository.findByIdAndGithubId(repoId,githubId)
                .orElseThrow(()->new NotFoundException("Repository Not Found"));
    }

    @Transactional(readOnly = true)
    public IndexStatusResponse status(UUID repoId, UUID githubId){
        GithubRepository repository=this.requiredOwned(repoId, githubId);
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
        IndexStatus status=repo.getIndexStatus()==null?
                IndexStatus.PENDING
                :repo.getIndexStatus();
        return GithubRepositoryResponse.builder()
                .id(repo.getId())
                .githubId(repo.getGithubId())
                .githubRepoId(repo.getGithubRepoId())
                .owner(repo.getOwner())
                .name(repo.getName())
                .fullName(repo.getFullName())
                .isPrivate(repo.isPrivate())
                .branch(repo.getBranch())
                .languages(repo.getLanguages())
                .htmlUrl(repo.getHtmlUrl())
                .description(repo.getDescription())
                .indexStatus(status)
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
