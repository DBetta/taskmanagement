package co.ke.ipsl.interview.taskmanagement.task.dto;

import co.ke.ipsl.interview.taskmanagement.task.model.Task;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Denis Gitonga
 */
public record TaskDto(
        UUID id,

        String title,

        String description,

        LocalDateTime dueDate
) {

    public static TaskDto of(Task task) {
        return new TaskDto(
                task.id(),
                task.title(),
                task.description(),
                task.dueDate()
        );
    }
}
