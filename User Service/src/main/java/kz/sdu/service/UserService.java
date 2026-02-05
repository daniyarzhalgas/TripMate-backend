package kz.sdu.service;

import kz.sdu.entity.UserProfile;
import kz.sdu.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userRepository;

    public UserProfile getCurrentUser(Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();

        String userId = jwt.getSubject(); // sub
        String email = jwt.getClaim("email");

        UserProfile user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user;
    }
}