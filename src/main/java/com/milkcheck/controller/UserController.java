package com.milkcheck.controller;

import com.milkcheck.dto.ChangePasswordRequest;
import com.milkcheck.dto.MessageResponse;
import com.milkcheck.dto.UpdateProfileRequest;
import com.milkcheck.dto.UserResponse;
import com.milkcheck.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public UserResponse getProfile(@AuthenticationPrincipal UserDetails principal) {
        return userService.getProfile(principal.getUsername());
    }

    @PutMapping
    public UserResponse updateProfile(@AuthenticationPrincipal UserDetails principal,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(principal.getUsername(), request);
    }

    @PutMapping("/password")
    public MessageResponse changePassword(@AuthenticationPrincipal UserDetails principal,
                                          @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getUsername(), request);
        return new MessageResponse("Password updated successfully");
    }
}