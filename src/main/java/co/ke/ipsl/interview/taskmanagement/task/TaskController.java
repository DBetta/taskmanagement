package co.ke.ipsl.interview.taskmanagement.task;

import co.ke.ipsl.interview.taskmanagement.task.dto.CreateTaskDto;
import co.ke.ipsl.interview.taskmanagement.task.dto.TaskDto;
import co.ke.ipsl.interview.taskmanagement.task.dto.UpdateTaskDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.created;

/**
 * @author Denis Gitonga
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    @GetMapping
    Flux<TaskDto> fetchAllTasks() {
        return taskService.fetchAllTasks();
    }

    @PostMapping
    Mono<ResponseEntity<TaskDto>> createTask(
            @RequestBody @Valid CreateTaskDto createTaskDto
    ) {
        return taskService.createTask(createTaskDto)
                .map(taskDto -> created(URI.create("tasks"))
                        .body(taskDto));
    }

    @PutMapping("/{taskId}")
    Mono<ResponseEntity<TaskDto>> updateTask(
            @PathVariable UUID taskId,
            @RequestBody @Valid UpdateTaskDto updateTaskDto) {
        return taskService.updateTask(taskId, updateTaskDto)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{taskId}")
    Mono<ResponseEntity<TaskDto>> deleteTask(
            @PathVariable UUID taskId
    ) {
        return taskService.deleteTask(taskId)
                .map(ResponseEntity::ok);
    }
}
