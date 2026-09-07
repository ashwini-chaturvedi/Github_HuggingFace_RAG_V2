package backend.hugitbackend.config;

import backend.hugitbackend.security.AuthDetailService;
import backend.hugitbackend.security.GithubOAuth2UserService;
import backend.hugitbackend.security.HuggingFaceOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.*;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig  {
    @Value("${app.frontend-url}")
    String frontendUrl;

    private final GithubOAuth2UserService githubOAuth2UserService;
    private final HuggingFaceOAuth2UserService huggingFaceOAuth2UserService;
    private final AuthDetailService authDetailService;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CorsFilter corsFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationSuccessHandler githubOAuth2SuccessHandler,
                                                   AuthenticationFailureHandler githubOAuth2FailureHandler,
                                                   AuthenticationSuccessHandler huggingFaceOAuth2SuccessHandler,
                                                   AuthenticationFailureHandler huggingFaceOAuth2FailureHandler) throws Exception{
        return http
                .cors(cors-> cors.configurationSource(corsConfigurationSource))
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
                        .successHandler(githubOAuth2SuccessHandler)
                        .failureHandler(githubOAuth2FailureHandler)

                )
                .oauth2Login(oauth->oauth
                        .userInfoEndpoint(userInfo->userInfo
                                .userService(huggingFaceOAuth2UserService)
                        )
                        .successHandler(huggingFaceOAuth2SuccessHandler)
                        .failureHandler(huggingFaceOAuth2FailureHandler)
                )
                .authenticationProvider(authenticationProvider())
                .logout(logout->logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value())))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("HUGIT_SESSION")
                )
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    @Bean
    AuthenticationSuccessHandler githubOAuth2SuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler=new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl(frontendUrl+"/auth/callback");

        return handler;
    }
    @Bean
    AuthenticationFailureHandler githubOAuth2FailureHandler() {
        SimpleUrlAuthenticationFailureHandler handler=new SimpleUrlAuthenticationFailureHandler();
        handler.setDefaultFailureUrl(frontendUrl+"/login?error=oauth_failed");

        return handler;
    }
    @Bean
    AuthenticationSuccessHandler huggingFaceOAuth2SuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler=new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl(frontendUrl+"/auth/callback");

        return handler;
    }
    @Bean
    AuthenticationFailureHandler huggingFaceOAuth2FailureHandler() {
        SimpleUrlAuthenticationFailureHandler handler=new SimpleUrlAuthenticationFailureHandler();
        handler.setDefaultFailureUrl(frontendUrl+"/login?error=oauth_failed");

        return handler;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider=new DaoAuthenticationProvider(authDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration){
        return authenticationConfiguration.getAuthenticationManager();
    }
}
