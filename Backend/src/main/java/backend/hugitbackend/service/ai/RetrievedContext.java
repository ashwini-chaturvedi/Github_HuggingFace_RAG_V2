package backend.hugitbackend.service.ai;

import backend.hugitbackend.dto.records.CitationDto;

import java.util.List;

public record RetrievedContext(
        List<CitationDto>citations,
        String contextText
) {
}
