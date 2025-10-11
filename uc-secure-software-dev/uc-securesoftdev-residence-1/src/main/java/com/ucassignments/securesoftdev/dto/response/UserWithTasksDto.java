package com.ucassignments.securesoftdev.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class UserWithTasksDto {
    private Long id;
    private String username;
    private List<TaskDto> tasks;
}
