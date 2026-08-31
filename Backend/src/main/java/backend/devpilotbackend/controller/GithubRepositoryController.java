package backend.devpilotbackend.controller;

import backend.devpilotbackend.dto.GithubRepositoryResponse;
import backend.devpilotbackend.dto.records.IndexStatusResponse;
import backend.devpilotbackend.entity.GithubRepository;
import backend.devpilotbackend.security.CurrentUser;
import backend.devpilotbackend.service.GitHubRepositoryService;
import backend.devpilotbackend.service.indexing.IndexingService;
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
    private final GitHubRepositoryService gitHubRepositoryService;
    private final IndexingService indexingService;

    @GetMapping
    public List<GithubRepositoryResponse>listRepos(
            @RequestParam(name="refresh",defaultValue = "true")boolean refresh){
        UUID userId= this.currentUser.require().getId();

        if(refresh){
            return this.gitHubRepositoryService.syncAndListRepositories(userId);

        }
        return this.gitHubRepositoryService.sortedList(userId);
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
