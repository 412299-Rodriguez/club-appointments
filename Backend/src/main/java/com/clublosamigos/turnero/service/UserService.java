package com.clublosamigos.turnero.service;

import com.clublosamigos.turnero.dto.request.UpdateProfileRequest;
import com.clublosamigos.turnero.dto.response.UserResponse;
import com.clublosamigos.turnero.exception.ResourceNotFoundException;
import com.clublosamigos.turnero.model.User;
import com.clublosamigos.turnero.model.User.UserRole;
import com.clublosamigos.turnero.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for user management operations
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get all users (excluding deleted users)
     *
     * @return List of UserResponse
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findByIsDeletedFalse().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     *
     * @param id User ID
     * @return UserResponse
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToResponse(user);
    }

    /**
     * Get user entity by ID (for internal use)
     *
     * @param id User ID
     * @return User entity
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Get user entity by email
     */
    @Transactional(readOnly = true)
    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * Get user profile by email
     */
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        return convertToResponse(getUserEntityByEmail(email));
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateUserProfile(String email, UpdateProfileRequest request) {
        User user = getUserEntityByEmail(email);
        user.setFullName(request.getFullName());
        user = userRepository.save(user);
        return convertToResponse(user);
    }

    /**
     * Get users by role
     *
     * @param role UserRole
     * @return List of UserResponse
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRoleAndIsDeletedFalse(role).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all trainers
     *
     * @return List of UserResponse
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllTrainers() {
        return getUsersByRole(UserRole.ENTRENADOR);
    }

    /**
     * Soft delete user
     *
     * @param id User ID
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setIsDeleted(true);
        userRepository.save(user);
    }

    /**
     * Update user role (SUPER_ADMIN only)
     *
     * @param id User ID
     * @param role New role
     * @return Updated UserResponse
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserResponse updateUserRole(Long id, UserRole role) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setRole(role);
        user = userRepository.save(user);
        return convertToResponse(user);
    }

    /**
     * Convert User entity to UserResponse DTO
     *
     * @param user User entity
     * @return UserResponse DTO
     */
    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
