package co.ke.ipsl.interview.taskmanagement.task.model;

import co.ke.ipsl.interview.taskmanagement.task.dto.CreateTaskDto;
import co.ke.ipsl.interview.taskmanagement.task.dto.UpdateTaskDto;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Denis Gitonga
 */
@Table(name = "tasks", schema = "tasks")
public record Task(
        @Id
        UUID id,

        String title,

        String description,

        @Column(value = "due_date")
        LocalDateTime dueDate,

        @CreatedDate
        @Column(value = "created_date")
        Instant createdAt,

        @LastModifiedDate
        @Column(value = "updated_date")
        Instant updatedAt,

        @Version
        int version
) {

    public Task withId(UUID taskId) {
        return new Task(
                taskId,
                title(),
                description(),
                dueDate(),
                createdAt(),
                updatedAt(),
                version()
        );
    }

    public static Task of(CreateTaskDto createTaskDto) {
        return new Task(
                null,
                createTaskDto.title(),
                createTaskDto.description(),
                createTaskDto.dueDate(),
                null,
                null,
                0
        );
    }

    public Task update(UpdateTaskDto updateTaskDto) {

        return new Task(
                id(),
                updateTaskDto.title(),
                updateTaskDto.description(),
                updateTaskDto.dueDate(),
                createdAt(),
                updatedAt(),
                version()
        );
    }
}
