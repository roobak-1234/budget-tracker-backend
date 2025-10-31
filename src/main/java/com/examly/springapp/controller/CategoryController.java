package com.examly.springapp.controller;

import com.examly.springapp.model.Category;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.CategoryRepository;
import com.examly.springapp.repository.UserRepository;
import com.examly.springapp.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<?> createCategory(@Valid @RequestBody Category category, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId()).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        category.setUser(user);
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STANDARD_USER', 'PREMIUM_USER', 'ADMIN', 'FINANCE_MANAGER')")
    public ResponseEntity<List<Category>> getUserCategories(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Category> categories = categoryRepository.findByUserId(userPrincipal.getId());
        return ResponseEntity.ok(categories);
    }
}