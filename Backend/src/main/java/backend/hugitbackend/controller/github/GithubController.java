package backend.hugitbackend.controller.github;

import backend.hugitbackend.service.github.GitHubRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GithubController {

    private final GitHubRepositoryService gitHubService;
}
