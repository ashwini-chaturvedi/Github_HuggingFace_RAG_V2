package backend.hugitbackend.controller.github;

import backend.hugitbackend.dto.github.GithubRepositoryResponse;
import backend.hugitbackend.dto.records.IndexStatusResponse;
import backend.hugitbackend.entity.github.GithubRepository;
import backend.hugitbackend.security.CurrentUser;
import backend.hugitbackend.service.github.GithubRepositoryService;
import backend.hugitbackend.service.indexing.IndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class GithubRepositoryController {

    private final CurrentUser currentUser;
    private final GithubRepositoryService gitHubRepositoryService;
    private final IndexingService indexingService;

    @GetMapping
    public List<GithubRepositoryResponse>listRepos(
            @RequestParam(name="refresh",defaultValue = "true")boolean refresh){
        UUID githubId= this.currentUser.require().getUser().getId();

        if(refresh){
            return this.gitHubRepositoryService.syncAndListRepositories(githubId);

        }
        return this.gitHubRepositoryService.sortedList(githubId);
    }

    @GetMapping("/{repoId}")
    public GithubRepositoryResponse getRepo(@PathVariable UUID repoId  ){
        UUID userId=currentUser.require().getId();
        return this.gitHubRepositoryService.mapResponse(this.gitHubRepositoryService.requiredOwned(repoId,userId));
    }

    @GetMapping("/{repoId}/status")
    public IndexStatusResponse status(@PathVariable UUID repoId){
        UUID userId=this.currentUser.require().getId();
        return this.gitHubRepositoryService.status(repoId,userId);
    }

    @PostMapping("/{id}/index")
    public ResponseEntity<GithubRepositoryResponse> index(@PathVariable UUID id){
        UUID userId=this.currentUser.require().getId();
        GithubRepository repository=this.indexingService.startIndexing(id,userId);
        this.indexingService.indexAsync(id,userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(this.gitHubRepositoryService.mapResponse(repository));
    }
}
