package backend.devpilotbackend.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class UserResponse {
    private UUID id;
    private long githubId;
    private String githubUserName;
    private String displayName;
    private String avatarURL;
    private String accessToken;
    private String tokenScope;

}
