package backend.devpilotbackend.config;

import backend.devpilotbackend.security.GithubOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig  {
    @Value("${app.frontend-url}")
    String frontendUrl;

    private final GithubOAuth2UserService githubOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,AuthenticationSuccessHandler oAuth2SuccessHandler,
                                                   AuthenticationFailureHandler oAuth2FailureHandler) throws Exception{
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf->csrf.disable())
                .sessionManagement(session->session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth->auth
                        .requestMatchers(
                                "/api/auth/login-url",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS,"/**")
                        .permitAll()
                        .requestMatchers("/api/**")
                        .authenticated()
                        .anyRequest()
                        .permitAll())
                .exceptionHandling(exception->exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2Login(oauth->oauth
                        .userInfoEndpoint(userInfo->userInfo
                                .userService(githubOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)

                )
                .logout(logout->logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value())))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("DEVPILOT_SESSION")
                )
                .build();
    }


    @Bean
    AuthenticationSuccessHandler oAuth2SuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler=new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl(frontendUrl+"/auth/callback");

        return handler;
    }
    @Bean
    AuthenticationFailureHandler oAuth2FailureHandler() {
        SimpleUrlAuthenticationFailureHandler handler=new SimpleUrlAuthenticationFailureHandler();
        handler.setDefaultFailureUrl(frontendUrl+"/login?error=oauth_failed");

        return handler;
    }
}
