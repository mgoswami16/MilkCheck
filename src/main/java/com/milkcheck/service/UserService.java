package com.milkcheck.service;

import com.milkcheck.dto.ChangePasswordRequest;
import com.milkcheck.dto.UpdateProfileRequest;
import com.milkcheck.dto.UserResponse;
import com.milkcheck.entity.User;
import com.milkcheck.exception.BadRequestException;
import com.milkcheck.exception.ResourceNotFoundException;
import com.milkcheck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Call from inside a transaction; the returned entity is attached there. */
    @Transactional(readOnly = true)
    public User getEntityByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        return UserResponse.from(getEntityByEmail(email));
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest req) {
        User user = getEntityByEmail(email);
        user.setFullName(req.fullName().trim());
        user.setPhone(req.phone());
        user.setAddressLine(req.addressLine());
        user.setCity(req.city());
        user.setState(req.state());
        user.setPostalCode(req.postalCode());
        log.info("Profile updated: {}", email);
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest req) {
        User user = getEntityByEmail(email);
        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(req.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password");
        }
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        log.info("Password changed: {}", email);
    }
}