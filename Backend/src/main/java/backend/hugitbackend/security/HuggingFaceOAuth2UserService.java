package backend.hugitbackend.security;

import backend.hugitbackend.entity.User;
import backend.hugitbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HuggingFaceOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>  {
    private final UserService userService;
    private final DefaultOAuth2UserService defaultOAuth2UserService=new DefaultOAuth2UserService();

    public @Nullable OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest){
        OAuth2User oAuth2User=this.defaultOAuth2UserService.loadUser(oAuth2UserRequest);

        String accessToken=oAuth2UserRequest.getAccessToken().getTokenValue();
        String scopes=oAuth2UserRequest.getAccessToken().getScopes()!=null?
                String.join(",",oAuth2UserRequest.getAccessToken().getScopes())
                :"read:user,repo";

        User user=new User();
    }

}
