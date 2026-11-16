package backend.hugitbackend.service.huggingface;

import backend.hugitbackend.dto.huggingface.HuggingFaceRequest;
import backend.hugitbackend.dto.huggingface.HuggingFaceResponse;
import backend.hugitbackend.entity.huggingface.HuggingFace;
import backend.hugitbackend.exceptions.NotFoundException;
import backend.hugitbackend.repository.huggingface.HuggingFaceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HuggingFaceService {

    private HuggingFaceRepo huggingFaceRepo;

    public HuggingFaceResponse requireById(UUID id){
         HuggingFace huggingFace=this.huggingFaceRepo.findById(id)
                .orElseThrow(()->new NotFoundException("Can't find Repository "));
        return this.mapHuggingFaceToHuggingFaceResponse(huggingFace);
    }

    //Helpers
    private HuggingFaceResponse mapHuggingFaceToHuggingFaceResponse(HuggingFace huggingFace){
        return HuggingFaceResponse.builder()
                .id(huggingFace.getId())
                .huggingFaceId(huggingFace.getHuggingFaceId())
                .userId(huggingFace.getUserId())
                .huggingFaceUserName(huggingFace.getHuggingFaceUserName())
                .displayName(huggingFace.getDisplayName())
                .avatarUrl(huggingFace.getAvatarUrl())
                .accessToken(huggingFace.getAccessToken())
                .refreshToken(huggingFace.getRefreshToken())
                .tokenScope(huggingFace.getTokenScope())
                .tokenExpiresAt(huggingFace.getTokenExpiresAt())
                .createdAt(huggingFace.getCreatedAt())
                .updatedAt(huggingFace.getUpdatedAt())
                .build();
    }

    private HuggingFace mapHuggingFaceRequestToHuggingFace(HuggingFaceRequest huggingFaceRequest){
        return HuggingFace.builder()
                .huggingFaceId(huggingFaceRequest.huggingFaceId())
                .userId(huggingFaceRequest.userId())
                .huggingFaceUserName(huggingFaceRequest.huggingFaceUserName())
                .displayName(huggingFaceRequest.displayName())
                .avatarUrl(huggingFaceRequest.avatarUrl())
                .accessToken(huggingFaceRequest.accessToken())
                .refreshToken(huggingFaceRequest.refreshToken())
                .tokenScope(huggingFaceRequest.tokenScope())
                .tokenExpiresAt(huggingFaceRequest.tokenExpiresAt())
                .build();
    }
}
