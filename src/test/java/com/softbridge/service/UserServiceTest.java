package com.softbridge.service;

import com.softbridge.dto.request.ChangePasswordRequest;
import com.softbridge.dto.request.UpdateUserRequest;
import com.softbridge.dto.response.UserResponse;
import com.softbridge.entity.User;
import com.softbridge.enums.Role;
import com.softbridge.exception.BadRequestException;
import com.softbridge.exception.ConflictException;
import com.softbridge.exception.ResourceNotFoundException;
import com.softbridge.repository.UserRepository;
import com.softbridge.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock UserRepository  userRepo;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks
    UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@test.com")
                .password("$2a$10$hashedpassword")
                .company("Acme Corp")
                .role(Role.CLIENT)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("getById() → returns correct UserResponse")
    void getById_shouldReturnUser() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john@test.com");
        assertThat(response.getFullName()).isEqualTo("John Smith");
    }

    @Test
    @DisplayName("getById() with unknown ID → throws ResourceNotFoundException")
    void getById_unknownId_throwsException() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getAll() without query → returns all users")
    void getAll_noQuery_returnsAll() {
        when(userRepo.findAll()).thenReturn(List.of(testUser));

        List<UserResponse> result = userService.getAll(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john@test.com");
    }

    @Test
    @DisplayName("getAll() with query → uses search method")
    void getAll_withQuery_usesSearch() {
        when(userRepo.search("john")).thenReturn(List.of(testUser));

        List<UserResponse> result = userService.getAll("john");

        assertThat(result).hasSize(1);
        verify(userRepo).search("john");
        verify(userRepo, never()).findAll();
    }

    @Test
    @DisplayName("update() → saves updated fields")
    void update_shouldSaveUpdatedFields() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepo.save(any())).thenReturn(testUser);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("Jane");
        req.setCompany("NewCorp");

        UserResponse result = userService.update(1L, req);

        verify(userRepo).save(argThat(u ->
                u.getFirstName().equals("Jane") && u.getCompany().equals("NewCorp")));
    }

    @Test
    @DisplayName("update() with conflicting email → throws ConflictException")
    void update_duplicateEmail_throwsConflict() {
        User other = User.builder().id(2L).email("taken@test.com").build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepo.existsByEmail("taken@test.com")).thenReturn(true);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setEmail("taken@test.com");

        assertThatThrownBy(() -> userService.update(1L, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    @DisplayName("changePassword() → encodes and saves new password")
    void changePassword_shouldEncodeAndSave() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldpass", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newpass123")).thenReturn("$2a$10$newhashedpassword");
        when(userRepo.save(any())).thenReturn(testUser);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("oldpass");
        req.setNewPassword("newpass123");
        req.setConfirmPassword("newpass123");

        userService.changePassword(1L, req);

        verify(userRepo).save(argThat(u ->
                u.getPassword().equals("$2a$10$newhashedpassword")));
    }

    @Test
    @DisplayName("changePassword() with wrong current password → throws BadRequestException")
    void changePassword_wrongCurrent_throwsException() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", testUser.getPassword())).thenReturn(false);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("wrongpass");
        req.setNewPassword("newpass123");
        req.setConfirmPassword("newpass123");

        assertThatThrownBy(() -> userService.changePassword(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("incorrect");
    }

    @Test
    @DisplayName("changePassword() with mismatched new passwords → throws BadRequestException")
    void changePassword_mismatch_throwsException() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldpass", testUser.getPassword())).thenReturn(true);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("oldpass");
        req.setNewPassword("newpass123");
        req.setConfirmPassword("different");

        assertThatThrownBy(() -> userService.changePassword(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    @DisplayName("toggleActive() → flips active boolean")
    void toggleActive_shouldFlipStatus() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepo.save(any())).thenReturn(testUser);

        userService.toggleActive(1L);

        // testUser was active=true, should become false
        verify(userRepo).save(argThat(u -> !u.getActive()));
    }

    @Test
    @DisplayName("delete() → calls repository delete")
    void delete_shouldCallRepoDelete() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepo).delete(any());

        userService.delete(1L);

        verify(userRepo).delete(testUser);
    }

    @Test
    @DisplayName("getMe() by email → returns user")
    void getMe_shouldReturnCurrentUser() {
        when(userRepo.findByEmail("john@test.com")).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getMe("john@test.com");

        assertThat(result.getEmail()).isEqualTo("john@test.com");
    }
}
