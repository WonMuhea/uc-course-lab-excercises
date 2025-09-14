package com.ucassignments.securesoftdev.api;

import com.ucassignments.securesoftdev.model.User;
import com.ucassignments.securesoftdev.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin")
    public String showAdminDashboard(Model model) {
        model.addAttribute("user", new User()); // For the create user form
        return "admin-dashboard";
    }

    @PostMapping("/admin/create-user")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        // ... (existing createUser logic)
        try {
            userService.createUser(user.getUsername(), user.getPassword(), user.getRoles());
            redirectAttributes.addFlashAttribute("successMessage", "User '" + user.getUsername() + "' created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin";
    }
}

