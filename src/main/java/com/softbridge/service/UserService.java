package com.softbridge.service;

import com.softbridge.dto.request.ChangePasswordRequest;
import com.softbridge.dto.request.UpdateUserRequest;
import com.softbridge.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse       getById(Long id);
    List<UserResponse> getAll(String query);
    UserResponse       update(Long id, UpdateUserRequest request);
    void               changePassword(Long id, ChangePasswordRequest request);
    void               toggleActive(Long id);
    void               delete(Long id);
    UserResponse       getMe(String email);
}
