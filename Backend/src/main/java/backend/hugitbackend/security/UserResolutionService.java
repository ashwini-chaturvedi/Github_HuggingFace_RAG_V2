package backend.hugitbackend.security;

import backend.hugitbackend.entity.User;
import backend.hugitbackend.exceptions.NoEmailFromProviderException;
import backend.hugitbackend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserResolutionService {
    private final UserRepo userRepo;

    public User resolveOrCreateUser(String email,String preferredUserName){
        if(email!=null){
            Optional<User>existing=this.userRepo.findByEmailId(email);
            return existing.orElseGet(() -> this.userRepo.save(
                    User.builder()
                            .emailId(email)
                            .userName(generateUniqueUserName(preferredUserName))
                            .createdAt(Instant.now())
                            .build()
            ));

        }
        throw new NoEmailFromProviderException(
                "Provider did not supply an email — cannot safely create or match a User account");
    }

    public String generateUniqueUserName(String prefUserName){
        String base=(prefUserName!=null && !prefUserName.isBlank())?prefUserName:"User";

        String candidate=base;
        int suffix = 1;
        while (this.userRepo.findByUserName(candidate).isPresent()) {
            candidate = base + suffix++;
        }
        return candidate;

    }
}
