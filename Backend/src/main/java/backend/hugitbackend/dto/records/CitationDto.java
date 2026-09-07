package backend.hugitbackend.dto.records;


public record CitationDto(
        String filePath,
        Integer startLine,
        Integer endLine,
        String language) {
}
