package backend.hugitbackend.dto;

import backend.hugitbackend.entity.github.Github;
import backend.hugitbackend.entity.huggingface.HuggingFace;
import lombok.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserRequest (

        UUID id,
        String emailId,
        String password,
        String userName,
        String description,
        long githubId,
        long huggingFaceId,
        Instant createdAt
)
{
}
