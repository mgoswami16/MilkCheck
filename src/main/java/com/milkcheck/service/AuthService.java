package com.milkcheck.service;

import com.milkcheck.dto.*;
import com.milkcheck.entity.Role;
import com.milkcheck.entity.User;
import com.milkcheck.exception.DuplicateResourceException;
import com.milkcheck.repository.RoleRepository;
import com.milkcheck.repository.UserRepository;
import com.milkcheck.security.CustomUserDetailsService;
import com.milkcheck.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("ROLE_CUSTOMER is missing"));

        User user = new User();
        user.setFullName(req.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setPhone(req.phone());
        user.getRoles().add(customerRole);
        userRepository.save(user);

        log.info("New customer registered: {}", email);
        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req, boolean adminOnly) {
        String email = req.email().trim().toLowerCase();

        // Throws BadCredentialsException / DisabledException, handled globally.
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.password()));

        if (adminOnly && auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            log.warn("Non-admin attempted admin login: {}", email);
            throw new AccessDeniedException("Admin access required");
        }

        User user = userRepository.findByEmail(email).orElseThrow();
        log.info("User logged in: {} (adminLogin={})", email, adminOnly);
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
        return AuthResponse.of(token, jwtService.getExpirationMs(), UserResponse.from(user));
    }
}