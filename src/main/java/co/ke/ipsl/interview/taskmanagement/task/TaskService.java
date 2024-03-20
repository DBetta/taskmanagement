package co.ke.ipsl.interview.taskmanagement.task;

import co.ke.ipsl.interview.taskmanagement.task.dto.CreateTaskDto;
import co.ke.ipsl.interview.taskmanagement.task.dto.TaskDto;
import co.ke.ipsl.interview.taskmanagement.task.dto.UpdateTaskDto;
import co.ke.ipsl.interview.taskmanagement.task.exception.TaskAlreadyExistException;
import co.ke.ipsl.interview.taskmanagement.task.exception.TaskMissingException;
import co.ke.ipsl.interview.taskmanagement.task.model.Task;
import co.ke.ipsl.interview.taskmanagement.task.model.TaskRepository;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author Denis Gitonga
 */
@Service
@EnableCaching
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Flux<TaskDto> fetchAllTasks() {
        return taskRepository.findAll()
                .map(TaskDto::of);
    }

    @Transactional
    public Mono<TaskDto> createTask(CreateTaskDto createTaskDto) {
        return taskRepository.findByTitle(createTaskDto.title())
                .hasElement()
                .flatMap(titleExist -> {
                    if (titleExist) {
                        return Mono.error(new TaskAlreadyExistException());
                    }
                    var newTask = Task.of(createTaskDto);
                    return taskRepository.save(newTask);
                })
                .map(TaskDto::of);
    }

    @Transactional
    public Mono<TaskDto> updateTask(final UUID taskId, final UpdateTaskDto updateTaskDto) {
        return taskRepository.findById(taskId)
                .switchIfEmpty(Mono.error(new TaskMissingException()))
                .map(task -> task.update(updateTaskDto))
                .flatMap(taskRepository::save)
                .map(TaskDto::of);
    }

    @Transactional
    public Mono<TaskDto> deleteTask(final UUID taskId) {
        return taskRepository.findById(taskId)
                .switchIfEmpty(Mono.error(new TaskMissingException()))
                .flatMap(task -> taskRepository.delete(task)
                        .thenReturn(task))
                .map(TaskDto::of);
    }
}
