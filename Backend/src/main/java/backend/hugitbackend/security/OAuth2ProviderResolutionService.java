package backend.hugitbackend.security;

import backend.hugitbackend.security.github.GithubOAuth2UserService;
import backend.hugitbackend.security.huggingface.HuggingFaceOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2ProviderResolutionService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final GithubOAuth2UserService githubOAuth2UserService;
    private final HuggingFaceOAuth2UserService huggingFaceOAuth2UserService;

    @Override
    public @Nullable OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId=userRequest.getClientRegistration().getRegistrationId();
        return switch (registrationId){
            case "github"->this.githubOAuth2UserService.loadUser(userRequest);
            case "huggingface"->this.huggingFaceOAuth2UserService.loadUser(userRequest);
            default -> throw new OAuth2AuthenticationException("Unsupported Provider:"+registrationId);
        };
    }
}
