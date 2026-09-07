package backend.hugitbackend.repository.huggingface;

import backend.hugitbackend.entity.huggingface.HuggingFace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HuggingFaceRepo extends JpaRepository<HuggingFace, UUID> {
}
