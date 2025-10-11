package com.ucassignments.securesoftdev.api;


import com.ucassignments.securesoftdev.dto.response.TaskDto;
import com.ucassignments.securesoftdev.dto.response.UserDto;
import com.ucassignments.securesoftdev.dto.response.UserWithTasksDto;
import com.ucassignments.securesoftdev.model.User;
import com.ucassignments.securesoftdev.repository.TaskRepository;
import com.ucassignments.securesoftdev.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public AdminController(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/users")
    public List<UserDto> users() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/users-with-tasks")
    public List<UserWithTasksDto> getAllUsersWithTasks() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserWithTasksDto dto = new UserWithTasksDto();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());

                    List<TaskDto> taskDtos = taskRepository.findByUser(user).stream()
                            .map(task -> {
                                TaskDto taskDto = new TaskDto();
                                taskDto.setId(task.getId());
                                taskDto.setTitle(task.getTitle());
                                taskDto.setDescription(task.getDescription());
                                return taskDto;
                            })
                            .collect(Collectors.toList());

                    dto.setTasks(taskDtos);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{userId}/tasks")
    public List<TaskDto> getTasksForUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return taskRepository.findByUser(user).stream()
                .map(TaskDto::fromEntity)
                .collect(Collectors.toList());
    }

}


