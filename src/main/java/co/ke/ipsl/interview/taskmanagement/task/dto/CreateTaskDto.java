package co.ke.ipsl.interview.taskmanagement.task.dto;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * @author Denis Gitonga
 */
public record CreateTaskDto(


        @NotNull(message = "title should not be null")
        @NotBlank(message = "title should not be blank")
        @Size(min = 5, max = 100, message = "title should have a length between {min} and {max} ")
        String title,

        @NotNull(message = "description should not be null")
        @NotBlank(message = "description should not be blank")
        String description,

        @Future(message = "Due date should be a day in the future")
        LocalDateTime dueDate
) {
}
