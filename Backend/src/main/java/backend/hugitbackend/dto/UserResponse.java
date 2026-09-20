package backend.hugitbackend.dto;

import backend.hugitbackend.entity.github.Github;
import backend.hugitbackend.entity.huggingface.HuggingFace;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID id,
        String emailId,
        String password,
        String userName,
        String description,
        long githubId,
        long huggingFaceId,
        boolean isGitHubConnected,
        boolean isHuggingFaceConnected,
        Instant createdAt

) {
}
