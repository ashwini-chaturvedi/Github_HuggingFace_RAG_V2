package backend.hugitbackend.service;

import backend.hugitbackend.dto.UserRequest;
import backend.hugitbackend.dto.UserResponse;
import backend.hugitbackend.entity.User;
import backend.hugitbackend.entity.github.Github;
import backend.hugitbackend.repository.github.GithubRepo;
import backend.hugitbackend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepository;
    private final GithubRepo githubRepo;
    public final TextEncryptor tokenEncryptor;

    @Transactional(readOnly = true)
    public UserResponse requiredById(UUID id){
        User user=this.userRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("Can't find user with id:"+id));

        return this.mapUserToUserResponse(user);
    }

    @Transactional
    public User upsertFromGitHub(Map<String, Object> attributes, String accessToken, String scopes) {
        Long githubId=toLong(attributes.get("id"));
        String login=String.valueOf(attributes.get("login"));
        String name=attributes.get("name")!=null
                ?String.valueOf(attributes.get("name"))
                :login;
        String avatarUrl=attributes.get("avatar_url")!=null?
                String.valueOf(attributes.get("avatar_url"))
                :null;
        String encryptToken=tokenEncryptor.encrypt(accessToken);

        User user=this.userRepository.findByGithubId(githubId).orElseGet(User::new);

        Github github= new Github();
        github.setGithubId(githubId);
        github.setGithubUserName(login);
        github.setAvatarURL(avatarUrl);
        github.setAccessToken(encryptToken);
        github.setDisplayName(name);
        github.setTokenScope(scopes);

        user.setGithub(github);


        return  this.userRepository.save(user);
    }
    @Transactional
    public User upsertFromHuggingFace(Map<String, Object> attributes, String accessToken, String scopes) {
        Long githubId=toLong(attributes.get("id"));
        String login=String.valueOf(attributes.get("login"));
        String name=attributes.get("name")!=null
                ?String.valueOf(attributes.get("name"))
                :login;
        String avatarUrl=attributes.get("avatar_url")!=null?
                String.valueOf(attributes.get("avatar_url"))
                :null;
        String encryptToken=tokenEncryptor.encrypt(accessToken);

        User user=this.userRepository.findByGithubId(githubId).orElseGet(User::new);

        Github github= new Github();
        github.setGithubId(githubId);
        github.setGithubUserName(login);
        github.setAvatarURL(avatarUrl);
        github.setAccessToken(encryptToken);
        github.setDisplayName(name);
        github.setTokenScope(scopes);

        user.setGithub(github);


        return  this.userRepository.save(user);
    }




    //helper functions

    private static Long toLong(Object value){
        if(value instanceof Number number){
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
    private UserResponse mapUserToUserResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .emailId(user.getEmailId())
                .userName(user.getUserName())
                .description(user.getDescription())
                .githubId(user.getGithubId())
                .huggingFaceId(user.getHuggingFaceId())
                .createdAt(user.getCreatedAt())
                .build();
    }
    private User mapUserRequestToUser(UserRequest userRequest){
        return User.builder()
                .emailId(userRequest.emailId())
                .password(userRequest.password())
                .userName(userRequest.userName())
                .description(userRequest.description())
                .githubId(userRequest.githubId())
                .huggingFaceId(userRequest.huggingFaceId())
                .build();
    }



}
