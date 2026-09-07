package backend.hugitbackend.controller;

import backend.hugitbackend.dto.UserResponse;
import backend.hugitbackend.exceptions.NotFoundException;
import backend.hugitbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id){
        try {
            return ResponseEntity.ok(this.userService.getUser(id));
        } catch (RuntimeException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}
