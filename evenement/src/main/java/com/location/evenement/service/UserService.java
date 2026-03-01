package com.location.evenement.service;

import com.location.evenement.dto.response.UserResponse;
import com.location.evenement.model.User;
import java.util.List;

public interface UserService {
    UserResponse getUserById(Long id);
    User getUserEntityById(Long id);
    UserResponse getUserByEmail(String email);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, User userDetails);
    void deleteUser(Long id);
    User getCurrentUser();
}