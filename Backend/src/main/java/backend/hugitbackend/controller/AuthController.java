package backend.hugitbackend.controller;

import backend.hugitbackend.dto.UserResponse;
import backend.hugitbackend.entity.User;
import backend.hugitbackend.security.AppUserPrincipal;
import backend.hugitbackend.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CurrentUser currentUser;

    @GetMapping("/github/login-url")
    public Map<String,String> loginUrl1(){
        return Map.of("url","/oauth2/authorization/github");
    }
    @GetMapping("/huggingface/login-url")
    public Map<String,String> loginUrl2(){
        return Map.of("url","/oauth2/authorization/huggingface");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(){
        AppUserPrincipal principal=currentUser.require();

        User user=principal.getUser();

        return ResponseEntity.ok(
                UserResponse.builder()
                        .id(user.getId())
                        .emailId(user.getEmailId())
                        .userName(user.getUserName())
                        .description(user.getDescription())
                        .githubId(user.getGithubId())
                        .huggingFaceId(user.getHuggingFaceId())
                        .createdAt(user.getCreatedAt())
                        .build()

        );
    }

}
