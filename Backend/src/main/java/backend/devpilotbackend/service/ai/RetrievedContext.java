package backend.devpilotbackend.service.ai;

import backend.devpilotbackend.dto.records.CitationDto;

import java.util.List;

public record RetrievedContext(
        List<CitationDto>citations,
        String contextText
) {
}
