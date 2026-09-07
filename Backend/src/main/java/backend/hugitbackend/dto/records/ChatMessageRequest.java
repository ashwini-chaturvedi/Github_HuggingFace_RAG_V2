package backend.hugitbackend.dto.records;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank
        String content
) {
}
