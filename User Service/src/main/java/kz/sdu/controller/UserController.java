package kz.sdu.controller;

import kz.sdu.entity.UserProfile;
import kz.sdu.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfile me(Authentication authentication) {
        return userService.getCurrentUser(authentication);
    }
}
