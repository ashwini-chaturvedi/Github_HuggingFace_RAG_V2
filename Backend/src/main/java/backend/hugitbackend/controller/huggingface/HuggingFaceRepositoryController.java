package backend.hugitbackend.controller.huggingface;

import backend.hugitbackend.service.huggingface.HuggingFaceRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/huggingface")
@RequiredArgsConstructor
public class HuggingFaceRepositoryController {
    private HuggingFaceRepositoryService huggingFaceRepositoryService;
}
