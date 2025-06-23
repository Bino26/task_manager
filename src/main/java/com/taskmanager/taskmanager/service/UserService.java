package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.request.UserRequest;
import com.taskmanager.taskmanager.dto.response.UserResponse;
import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.entity.enums.Role;
import com.taskmanager.taskmanager.exception.custom.CustomAccessDeniedException;
import com.taskmanager.taskmanager.exception.custom.CustomAlreadyExistException;
import com.taskmanager.taskmanager.exception.custom.CustomNotFoundException;
import com.taskmanager.taskmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public List<UserResponse> listAll() {
        logger.info("Listing all users");
        List<UserResponse> users = userRepository.findAllByDeletedAtNull().stream()
                .map(UserResponse::from)
                .toList();
        logger.info("Found {} users", users.size());
        return users;
    }

    public UserResponse getById(UUID id) {
        logger.info("Fetching user by id: {}", id);
        User user = findById(id);
        logger.info("User found with id: {}", id);
        return UserResponse.from(user);
    }

    public UserResponse save(User user) {
        logger.info("Saving user with email: {}", user.getEmail());
        existsByEmail(user.getEmail());
        user.setAuthorities(Set.of(Role.TEAM_MEMBER));
        User savedUser = userRepository.save(user);
        logger.info("User saved with id: {}", savedUser.getId());
        return UserResponse.from(savedUser);
    }

    public UserResponse update(UUID id, UserRequest userRequest) {
        logger.info("Updating user with id: {}", id);
        User user = findById(id);

        if (!Objects.equals(user.getEmail(), getAuthenticatedUser().getEmail())){
            logger.error("User is not authorized to update user with id: {}", id);
            throw new CustomAccessDeniedException("User is not authorized to update user with id: " + id);
        }

        if (!user.getEmail().equals(userRequest.email())) {
            existsByEmail(userRequest.email());
        }

        user.setName(userRequest.name());
        user.setEmail(userRequest.email());
        user.setPassword(userRequest.password());
        User updatedUser = userRepository.save(user);
        logger.info("User updated with id: {}", updatedUser.getId());
        return UserResponse.from(updatedUser);
    }

    public UserResponse addRole(UUID id, Role role) {
        logger.info("Adding role {} to user with id: {}", role, id);
        User user = findById(id);
        if (user.getAuthorities().contains(role)) {
            logger.info("User already has role: {}", role);
            return UserResponse.from(user);
        }
        user.getAuthorities().add(role);
        User updatedUser = userRepository.save(user);
        logger.info("Role {} added to user with id: {}", role, updatedUser.getId());
        return UserResponse.from(updatedUser);
    }

    public UserResponse removeRole(UUID id, Role role) {
        logger.info("Removing role {} from user with id: {}", role, id);
        User user = findById(id);
        if (!user.getAuthorities().contains(role)) {
            logger.warn("User does not have role: {}", role);
            throw new CustomNotFoundException("User does not have role: " + role);
        }
        if (role == Role.TEAM_MEMBER) {
            logger.info("Role TEAM_MEMBER cannot be removed");
            return UserResponse.from(user);
        }

        user.getAuthorities().remove(role);
        User updatedUser = userRepository.save(user);
        logger.info("Role {} removed from user with id: {}", role, updatedUser.getId());
        return UserResponse.from(updatedUser);
    }

    @Transactional
    public void delete(UUID id) {
        logger.info("Deleting user with id: {}", id);
        User user = findById(id);
        user.softDelete();
        userRepository.save(user);
        tokenService.deleteRefreshTokenByUserId(id);
        logger.info("User deleted with id: {}", id);
    }

    protected User findById(UUID id) {
        logger.debug("Finding user by id: {}", id);
        return userRepository.findByIdAndDeletedAtNull(id).orElseThrow(
                () -> {
                    logger.error("User not found with id: {}", id);
                    return new CustomNotFoundException("User not found with id: " + id);
                });
    }

    protected User findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userRepository.findByEmailAndDeletedAtNull(email).orElseThrow(
                () -> {
                    logger.error("User not found with email: {}", email);
                    return new CustomNotFoundException("User not found with email: " + email);
                });
    }

    protected User getAuthenticatedUser() {
        String email =  SecurityContextHolder.getContext().getAuthentication().getName();
        return findByEmail(email);
    }

    protected void existsByEmail(String email) {
        logger.debug("Checking if user exists with email: {}", email);
        if (userRepository.existsByEmailAndDeletedAtNull(email)) {
            logger.error("User already exists with email: {}", email);
            throw new CustomAlreadyExistException("User already exist with email: " + email);
        }
    }
}