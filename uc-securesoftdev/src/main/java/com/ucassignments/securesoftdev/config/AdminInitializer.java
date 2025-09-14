package com.ucassignments.securesoftdev.config;

import com.ucassignments.securesoftdev.model.User;
import com.ucassignments.securesoftdev.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner initializeAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("addmin123"));
                admin.setRoles("ROLE_ADMIN,ROLE_USER");
                userRepository.save(admin);
            }
        };
    }
}

