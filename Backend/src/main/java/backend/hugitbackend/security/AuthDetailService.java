package backend.hugitbackend.security;

import backend.hugitbackend.dto.AuthPrincipal;
import backend.hugitbackend.entity.User;
import backend.hugitbackend.exceptions.NotFoundException;
import backend.hugitbackend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthDetailService implements UserDetailsService {
    private final UserRepo userRepo;


    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String userName) throws UsernameNotFoundException {
        User user=this.userRepo.findByUserName(userName).orElseThrow(NotFoundException::new);

        return new AuthPrincipal(user);
    }
}
