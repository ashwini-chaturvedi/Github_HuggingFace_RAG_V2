package backend.devpilotbackend.service.ai;

import backend.devpilotbackend.dto.records.ChatMessageResponse;
import backend.devpilotbackend.dto.records.CitationDto;
import backend.devpilotbackend.entity.ChatMessage;
import backend.devpilotbackend.entity.MessageRole;
import backend.devpilotbackend.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatStreamHandler {

    //SHORT POLLING
    private final ChatModel chatModel;
    private final ChatMessageRepository chatMessageRepository;
    private final CitationMapper citationMapper;

    public SseEmitter stream(
            UUID sessionId, ChatMessageResponse savedUserMessage,
            List<CitationDto>citations,
            String systemPrompt,
            String userPrompt
            ){
        SseEmitter sseEmitter=new SseEmitter(RAG_Settings.STREAM_TIMEOUT_MS);
        StringBuilder fullReply=new StringBuilder();

        try {
            sseEmitter.send(SseEmitter.event()
                    .name("user_message")
                    .data(savedUserMessage));

            ChatClient.builder(chatModel)
                    .build()
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .stream()
                    .content()
                    .doOnNext(token -> appendToken(sseEmitter, fullReply, token))
                    .doOnError(err -> {
                        log.error("Chat stream error", err);
                        sseEmitter.completeWithError(err);
                    })
                    .doOnComplete(() -> completeStream(
                            sseEmitter, sessionId, fullReply, citations))
                    .subscribe();
        } catch (Exception ex) {
            sseEmitter.completeWithError(ex);
        }

        return sseEmitter;
    }

    private void appendToken(SseEmitter emitter, StringBuilder fullReply, String token) {
        fullReply.append(token);
        try {
            emitter.send(SseEmitter.event()
                    .name("token")
                    .data(token, MediaType.APPLICATION_JSON));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void completeStream(
            SseEmitter emitter,
            UUID sessionId,
            StringBuilder fullReply,
            List<CitationDto> citations) {
        try {
            ChatMessage assistant = this.chatMessageRepository.save(ChatMessage.builder()
                    .sessionId(sessionId)
                    .role(MessageRole.ASSISTANT)
                    .content(fullReply.toString())
                    .citations(citationMapper.toJson(citations))
                    .build());

            emitter.send(SseEmitter.event()
                    .name("assistant_message")
                    .data(toMessageResponse(assistant)));
            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
            emitter.complete();
        } catch (Exception ex) {
            emitter.completeWithError(ex);
        }
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                citationMapper.fromJson(message.getCitations()),
                message.getCreatedAt());
    }
}
