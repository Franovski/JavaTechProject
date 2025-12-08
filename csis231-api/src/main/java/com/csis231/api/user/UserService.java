
package com.csis231.api.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing {@link User} entities.
 * Handles business rules such as password hashing and unique checks.
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Returns all users. */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** Retrieves a user by ID. */
    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Creates a new user.  This will throw an IllegalArgumentException
     * if the username or email is already in use.  The password is hashed
     * before the entity is persisted.
     */
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already in use");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Updates an existing user.  Only fields that are non‑null on the
     * {@code updated} object will be applied.  If a new password is provided,
     * it will be hashed before persisting.
     */
    @Transactional
    public Optional<User> updateUser(Long id, User updated) {
        return userRepository.findById(id).map(existing -> {
            // username change
            if (updated.getUsername() != null && !updated.getUsername().equals(existing.getUsername())) {
                if (userRepository.existsByUsername(updated.getUsername())) {
                    throw new IllegalArgumentException("Username already in use");
                }
                existing.setUsername(updated.getUsername());
            }
            // email change
            if (updated.getEmail() != null && !updated.getEmail().equals(existing.getEmail())) {
                if (userRepository.existsByEmail(updated.getEmail())) {
                    throw new IllegalArgumentException("Email already in use");
                }
                existing.setEmail(updated.getEmail());
            }
            // password change
            if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(updated.getPassword()));
            }
            // update other properties if present
            if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
            if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
            if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
            if (updated.getIsActive() != null) existing.setIsActive(updated.getIsActive());
            if (updated.getEmailVerified() != null) existing.setEmailVerified(updated.getEmailVerified());
            if (updated.getTwoFactorEnabled() != null) existing.setTwoFactorEnabled(updated.getTwoFactorEnabled());
            if (updated.getRole() != null) existing.setRole(updated.getRole());
            return userRepository.save(existing);
        });
    }

    /** Deletes a user by ID.  Returns true if the user existed and was removed. */
    @Transactional
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
