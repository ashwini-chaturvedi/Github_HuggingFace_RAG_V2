package backend.hugitbackend.service;

import backend.hugitbackend.dto.UserRequest;
import backend.hugitbackend.dto.UserResponse;
import backend.hugitbackend.entity.User;
import backend.hugitbackend.entity.github.Github;
import backend.hugitbackend.repository.github.GithubRepo;
import backend.hugitbackend.repository.UserRepo;
import backend.hugitbackend.repository.huggingface.HuggingFaceRepo;
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
    private final HuggingFaceRepo huggingFaceRepo;
    public final TextEncryptor tokenEncryptor;

    @Transactional(readOnly = true)
    public UserResponse requiredById(UUID id){
        User user=this.userRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("Can't find user with id:"+id));

        return this.mapUserToUserResponse(user);
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
                .isGitHubConnected(this.githubRepo.findByUserId(user.getId()).isPresent())
                .isHuggingFaceConnected(this.huggingFaceRepo.findByUserId(user.getId()).isPresent())
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
