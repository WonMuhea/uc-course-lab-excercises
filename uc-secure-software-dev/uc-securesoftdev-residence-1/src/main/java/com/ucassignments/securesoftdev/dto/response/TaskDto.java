package com.ucassignments.securesoftdev.dto.response;
import com.ucassignments.securesoftdev.model.Task;
import lombok.Data;

@Data
public class TaskDto {
    private Long id;
    private String title;
    private String description;

    public static TaskDto fromEntity(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        return dto;
    }
}

