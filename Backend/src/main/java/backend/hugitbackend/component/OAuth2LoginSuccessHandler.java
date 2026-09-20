package backend.hugitbackend.component;

import backend.hugitbackend.dto.ProviderTokenData;
import backend.hugitbackend.entity.User;
import backend.hugitbackend.security.UserResolutionService;
import backend.hugitbackend.security.github.GithubConnectionService;
import backend.hugitbackend.security.huggingface.HuggingFaceConnectionService;
import backend.hugitbackend.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserResolutionService userResolutionService;
    private final GithubConnectionService githubConnectionService;
    private final HuggingFaceConnectionService huggingFaceConnectionService;
    private final JwtService jwtService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.frontend-url}")
    private String frontendUrl;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) authentication;
        String registrationId = oAuth2Token.getAuthorizedClientRegistrationId(); // "github" or "huggingface"
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String accessToken = resolveAccessToken(oAuth2Token);

        User user;

        if("github".equals(registrationId)){
            String email=(String)oAuth2User.getAttributes().get("email");
            String login=(String)oAuth2User.getAttributes().get("login");

            user=this.userResolutionService.resolveOrCreateUser(email,login);

            this.githubConnectionService.upsert(user.getId(),new ProviderTokenData(
                   String.valueOf(( (Number) oAuth2User.getAttributes().get("id")).longValue()),
                    login,
                    (String) oAuth2User.getAttributes().get("name"),
                    (String) oAuth2User.getAttributes().get("avatar_url"),
                    accessToken,null,null,"read:user,repo"
            ));
        }else if("huggingface".equals(registrationId)){
            try {
                System.out.println("Hugging Face");
                String hfId = (String) oAuth2User.getAttributes().get("id");
                String email=(String)oAuth2User.getAttributes().get("email");
                String username=(String)oAuth2User.getAttributes().get("name");
                String fullName = (String) oAuth2User.getAttributes().get("fullname");
                String avatarUrl = (String) oAuth2User.getAttributes().get("avatarUrl");
                
                if(email==null){
                    response.sendRedirect(frontendUrl + "/login?error=hf_no_email");
                    return;
                }
                user=this.userResolutionService.resolveOrCreateUser(email,username);

                this.huggingFaceConnectionService.upsert(user.getId(),new ProviderTokenData(
                        hfId,username,fullName,avatarUrl,
                        accessToken,null,null,"openid,profile,email,read-repos,gated-repos"
                ));
            }catch (Exception e){
                System.err.println("HF LOGIN ERROR: " + e.getMessage());
                e.printStackTrace();
                throw e;  // re-throw so it hits the failure handler
            }


        }else{
            throw new IllegalStateException("Unknown Provider:"+registrationId);
        }
        String jwt = jwtService.generateToken(user);
        response.sendRedirect(frontendUrl + "/auth/callback?token=" + jwt);
    }

    private String resolveAccessToken(OAuth2AuthenticationToken token) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                token.getAuthorizedClientRegistrationId(),
                token.getName()
        );
        if (client == null) {
            throw new IllegalStateException("No authorized client found for " + token.getAuthorizedClientRegistrationId());
        }
        return client.getAccessToken().getTokenValue();
    }


}
