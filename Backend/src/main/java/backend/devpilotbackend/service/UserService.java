package backend.devpilotbackend.service;

import backend.devpilotbackend.dto.UserRequest;
import backend.devpilotbackend.dto.UserResponse;
import backend.devpilotbackend.entity.User;
import backend.devpilotbackend.repository.UserRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
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

        User user=userRepository.findByGithubId(githubId).orElseGet(User::new);

        user.setGithubId(githubId);
        user.setGithubUserName(login);
        user.setAvatarURL(avatarUrl);
        user.setAccessToken(encryptToken);
        user.setDisplayName(name);
        user.setTokenScope(scopes);

        return  this.userRepository.save(user);
    }




    //helper functions


    public String decryptAccessToken(UserResponse user){
        return this.tokenEncryptor.decrypt(user.getAccessToken());
    }
    private static Long toLong(Object value){
        if(value instanceof Number number){
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
    private UserResponse mapUserToUserResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .githubId(user.getGithubId())
                .githubUserName(user.getGithubUserName())
                .displayName(user.getDisplayName())
                .avatarURL(user.getAvatarURL())
                .accessToken(user.getAccessToken())
                .tokenScope(user.getTokenScope())
                .build();
    }
    private User mapUserRequestToUser(UserRequest userRequest){
        return User.builder()
                .githubId(userRequest.getGithubId())
                .githubUserName(userRequest.getGithubUserName())
                .displayName(userRequest.getDisplayName())
                .avatarURL(userRequest.getAvatarURL())
                .accessToken(userRequest.getAccessToken())
                .tokenScope(userRequest.getTokenScope())
                .build();
    }



}
