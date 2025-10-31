package com.examly.springapp.config;

import com.examly.springapp.model.Category;
import com.examly.springapp.model.Role;
import com.examly.springapp.repository.CategoryRepository;
import com.examly.springapp.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, Role.ERole.ROLE_ADMIN));
            roleRepository.save(new Role(null, Role.ERole.ROLE_STANDARD_USER));
            roleRepository.save(new Role(null, Role.ERole.ROLE_PREMIUM_USER));
            roleRepository.save(new Role(null, Role.ERole.ROLE_FINANCE_MANAGER));
        }
        
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category(null, "Food", "Food and dining expenses", null));
            categoryRepository.save(new Category(null, "Transportation", "Transportation and travel expenses", null));
            categoryRepository.save(new Category(null, "Entertainment", "Entertainment and leisure expenses", null));
            categoryRepository.save(new Category(null, "Utilities", "Utility bills and services", null));
            categoryRepository.save(new Category(null, "Other", "Other miscellaneous expenses", null));
        }
    }
}