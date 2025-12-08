

package com.csis231.api.user;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller exposing CRUD endpoints for {@link User} entities.
 * The base path /api/csis-users avoids clashing with the standard
 * LearnOnline user controller already present in the application.
 */
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Lists all users. */
    @GetMapping
    public List<User> list() {
        return userService.getAllUsers();
    }

    /** Retrieves a user by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        Optional<User> user = userService.getUser(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Creates a new user. Expects a complete User in the body. */
    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.status(201).body(created);
    }

    /** Updates an existing user. Only non-null fields will be updated. */
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id,
                                       @Valid @RequestBody User user) {
        Optional<User> updated = userService.updateUser(id, user);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Deletes a user by ID. Returns 204 on success or 404 if not found. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = userService.deleteUser(id);
        return removed ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
    // in UserController (or AuthController)
    @GetMapping("/me")
    public com.csis231.api.user.MeResponse me(Authentication authentication) {
        String username = authentication.getName();
        User u = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));

        return new com.csis231.api.user.MeResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getPhone(),
                u.getRole().name()
        );
    }


}
