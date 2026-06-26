package com.softbridge.service.impl;

import com.softbridge.dto.request.ChangePasswordRequest;
import com.softbridge.dto.request.UpdateUserRequest;
import com.softbridge.dto.response.UserResponse;
import com.softbridge.entity.User;
import com.softbridge.exception.BadRequestException;
import com.softbridge.exception.ConflictException;
import com.softbridge.exception.ResourceNotFoundException;
import com.softbridge.repository.UserRepository;
import com.softbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return UserResponse.from(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll(String query) {
        List<User> users = (query != null && !query.isBlank())
                ? userRepo.search(query.trim())
                : userRepo.findAll();
        return users.stream().map(UserResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = findById(id);

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName()  != null) user.setLastName(request.getLastName());
        if (request.getCompany()   != null) user.setCompany(request.getCompany());
        if (request.getPhone()     != null) user.setPhone(request.getPhone());
        if (request.getActive()    != null) user.setActive(request.getActive());

        // Check for email conflict
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepo.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail().toLowerCase());
        }

        userRepo.save(user);
        log.info("User {} updated", id);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = findById(id);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New passwords do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
        log.info("Password changed for user {}", id);
    }

    @Override
    @Transactional
    public void toggleActive(Long id) {
        User user = findById(id);
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        userRepo.save(user);
        log.info("User {} active status → {}", id, user.getActive());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        userRepo.delete(user);
        log.info("User {} deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMe(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return UserResponse.from(user);
    }

    private User findById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
