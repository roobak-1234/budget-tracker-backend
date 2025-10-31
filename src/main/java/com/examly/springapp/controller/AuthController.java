package com.examly.springapp.controller;

import com.examly.springapp.dto.JwtResponse;
import com.examly.springapp.dto.LoginRequest;
import com.examly.springapp.dto.ProfileUpdateRequest;
import com.examly.springapp.dto.SignupRequest;
import com.examly.springapp.model.Role;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.RoleRepository;
import com.examly.springapp.repository.UserRepository;
import com.examly.springapp.security.JwtUtils;
import com.examly.springapp.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Check if user exists and is active before authentication
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .or(() -> userRepository.findByEmail(loginRequest.getUsername()))
                .orElse(null);
        if (user != null && !user.getIsActive()) {
            return ResponseEntity.badRequest().body("Your account is deactivated. Please contact support.");
        }
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        user = userRepository.findById(userDetails.getId()).orElse(null);
        String firstName = user != null ? user.getFirstName() : "";
        String lastName = user != null ? user.getLastName() : "";
        String currency = user != null ? user.getCurrency() : "USD";

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                firstName,
                lastName,
                currency,
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            System.out.println("Registration request: " + signUpRequest.toString());
            
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity.badRequest().body("Error: Username is already taken!");
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Error: Email is already in use!");
            }

            User user = new User();
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
            user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));
            user.setFirstName(signUpRequest.getFirstName());
            user.setLastName(signUpRequest.getLastName());
            user.setCurrency(signUpRequest.getCurrency() != null ? signUpRequest.getCurrency() : "INR");

            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(Role.ERole.ROLE_STANDARD_USER)
                    .orElse(null);
            
            if (userRole == null) {
                userRole = new Role(null, Role.ERole.ROLE_STANDARD_USER);
                userRole = roleRepository.save(userRole);
                System.out.println("Created new role: " + userRole.getName());
            }
            roles.add(userRole);

            user.setRoles(roles);
            User savedUser = userRepository.save(user);
            System.out.println("User saved with ID: " + savedUser.getId());

            return ResponseEntity.ok("User registered successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest profileRequest, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(userPrincipal.getId()).orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            // Check if email is being changed and if it's already taken by another user
            if (!user.getEmail().equals(profileRequest.getEmail()) && 
                userRepository.existsByEmail(profileRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Error: Email is already in use!");
            }

            user.setFirstName(profileRequest.getFirstName());
            user.setLastName(profileRequest.getLastName());
            user.setEmail(profileRequest.getEmail());
            user.setCurrency(profileRequest.getCurrency() != null ? profileRequest.getCurrency() : "INR");

            userRepository.save(user);
            return ResponseEntity.ok("Profile updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Profile update failed: " + e.getMessage());
        }
    }

    @PostMapping("/signup-admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity.badRequest().body("Error: Username is already taken!");
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Error: Email is already in use!");
            }

            User user = new User();
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
            user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));
            user.setFirstName(signUpRequest.getFirstName());
            user.setLastName(signUpRequest.getLastName());
            user.setCurrency(signUpRequest.getCurrency() != null ? signUpRequest.getCurrency() : "INR");

            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
                    .orElse(null);
            
            if (adminRole == null) {
                adminRole = new Role(null, Role.ERole.ROLE_ADMIN);
                adminRole = roleRepository.save(adminRole);
            }
            roles.add(adminRole);

            user.setRoles(roles);
            userRepository.save(user);

            return ResponseEntity.ok("Admin registered successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Admin registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String newPassword = request.get("newPassword");
            
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found with this email");
            }
            
            user.setPasswordHash(encoder.encode(newPassword));
            userRepository.save(user);
            
            return ResponseEntity.ok("Password reset successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Password reset failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-account")
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<?> deleteAccount(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(userPrincipal.getId()).orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            userRepository.delete(user);
            return ResponseEntity.ok("Account deleted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Account deletion failed: " + e.getMessage());
        }
    }
}