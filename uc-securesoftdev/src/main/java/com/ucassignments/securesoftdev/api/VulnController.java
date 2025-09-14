package com.ucassignments.securesoftdev.api;

import com.ucassignments.securesoftdev.model.User;
import com.ucassignments.securesoftdev.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class VulnController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Secure endpoint using JPA
    @Autowired
    private UserRepository userRepository;


    // Vulnerable endpoint for educational purposes
    @GetMapping("/api/unsafe/user")
    public List<User> unsafeSqlInjection(@RequestParam String username) {
        String sql = "SELECT id, username, roles FROM users WHERE username = '" + username + "'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setRoles(rs.getString("roles"));
            return user;
        });
    }

    // For testing rate limiter
    @GetMapping("/api/public/ping")
    public String ping() {
        return "Pong!";
    }

    @GetMapping("/api/secure/user")
    public Optional<User> secureApi(@RequestParam String username) {
        return userRepository.findByUsername(username);
    }
}
