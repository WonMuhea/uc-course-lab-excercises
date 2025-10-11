package com.ucassignments.securesoftdev.api;

import com.ucassignments.securesoftdev.model.Task;
import com.ucassignments.securesoftdev.model.User;
import com.ucassignments.securesoftdev.repository.TaskRepository;
import com.ucassignments.securesoftdev.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    public UserController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/tasks")
    public List<Task> getUserTasks() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        return taskRepository.findByUser(user);
    }

    @PostMapping("/tasks")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Task createTask(@RequestBody Task task) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        task.setUser(user);
        return taskRepository.save(task);
    }
}
