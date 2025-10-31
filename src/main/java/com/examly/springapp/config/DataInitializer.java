package com.examly.springapp.config;

import com.examly.springapp.model.Role;
import com.examly.springapp.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;
    


    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, Role.ERole.ROLE_ADMIN));
            roleRepository.save(new Role(null, Role.ERole.ROLE_STANDARD_USER));
            roleRepository.save(new Role(null, Role.ERole.ROLE_PREMIUM_USER));
            roleRepository.save(new Role(null, Role.ERole.ROLE_FINANCE_MANAGER));
        }
        
        // Categories are user-specific and will be created when users register
    }
}