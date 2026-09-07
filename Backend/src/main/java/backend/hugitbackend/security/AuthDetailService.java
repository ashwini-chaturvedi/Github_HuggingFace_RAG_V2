package backend.hugitbackend.security;

import backend.hugitbackend.dto.AuthPrincipal;
import backend.hugitbackend.entity.User;
import backend.hugitbackend.exceptions.NotFoundException;
import backend.hugitbackend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthDetailService implements UserDetailsService {
    private final UserRepo userRepo;


    @Override
    public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException {
        User user=this.userRepo.findByEmailId(emailId).orElseThrow(NotFoundException::new);

        return new AuthPrincipal(user);
    }
}
