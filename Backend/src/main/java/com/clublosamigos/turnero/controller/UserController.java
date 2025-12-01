package com.clublosamigos.turnero.controller;

import com.clublosamigos.turnero.dto.request.UpdateProfileRequest;
import com.clublosamigos.turnero.dto.response.UserResponse;
import com.clublosamigos.turnero.model.User;
import com.clublosamigos.turnero.model.User.UserRole;
import com.clublosamigos.turnero.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for user management operations
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get all users (SUPER_ADMIN only)
     *
     * @return List of UserResponse
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     *
     * @param id User ID
     * @return UserResponse
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR', 'ROLE_USUARIO')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get authenticated user profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getProfile() {
        String email = getAuthenticatedEmail();
        UserResponse response = userService.getUserProfile(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Update authenticated user profile
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        String email = getAuthenticatedEmail();
        UserResponse response = userService.updateUserProfile(email, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all trainers
     *
     * @return List of UserResponse
     */
    @GetMapping("/trainers")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR', 'ROLE_USUARIO')")
    public ResponseEntity<List<UserResponse>> getAllTrainers() {
        List<UserResponse> trainers = userService.getAllTrainers();
        return ResponseEntity.ok(trainers);
    }

    /**
     * Update user role (SUPER_ADMIN only)
     *
     * @param id User ID
     * @param role New role
     * @return Updated UserResponse
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id, @RequestParam UserRole role) {
        UserResponse user = userService.updateUserRole(id, role);
        return ResponseEntity.ok(user);
    }

    /**
     * Delete user (SUPER_ADMIN only)
     *
     * @param id User ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
