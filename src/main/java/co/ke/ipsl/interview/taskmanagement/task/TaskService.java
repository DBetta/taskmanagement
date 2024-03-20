package co.ke.ipsl.interview.taskmanagement.task;

import co.ke.ipsl.interview.taskmanagement.task.dto.CreateTaskDto;
import co.ke.ipsl.interview.taskmanagement.task.dto.FilterTaskDto;
import co.ke.ipsl.interview.taskmanagement.task.dto.TaskDto;
import co.ke.ipsl.interview.taskmanagement.task.dto.UpdateTaskDto;
import co.ke.ipsl.interview.taskmanagement.task.exception.TaskAlreadyExistException;
import co.ke.ipsl.interview.taskmanagement.task.exception.TaskMissingException;
import co.ke.ipsl.interview.taskmanagement.task.model.Task;
import co.ke.ipsl.interview.taskmanagement.task.model.TaskRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.exact;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;
import static org.springframework.data.relational.core.query.Criteria.*;
import static org.springframework.data.relational.core.query.Query.query;

/**
 * @author Denis Gitonga
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final R2dbcEntityTemplate template;

    public TaskService(TaskRepository taskRepository, R2dbcEntityTemplate template) {
        this.taskRepository = taskRepository;
        this.template = template;
    }

    @Cacheable(value = "tasks")
    public Mono<Page<TaskDto>> fetchTasks(
            FilterTaskDto filterTaskDto,
            Pageable pageable
    ) {

        final var criteria = filterTaskDto.criteria();
        final var tasksMono = template.select(Task.class).matching(query(criteria).with(pageable))
                .all()
                .map(TaskDto::of)
                .collectList();
       final var taskCountMono = template.count(query(criteria), Task.class);

       return Mono.zip(tasksMono, taskCountMono,
               (tasks, taskCount) -> new PageImpl<>(tasks, pageable, taskCount));
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
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
    @CacheEvict(value = "tasks", allEntries = true)
    public Mono<TaskDto> updateTask(final UUID taskId, final UpdateTaskDto updateTaskDto) {
        return taskRepository.findById(taskId)
                .switchIfEmpty(Mono.error(new TaskMissingException()))
                .map(task -> task.update(updateTaskDto))
                .flatMap(taskRepository::save)
                .map(TaskDto::of);
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public Mono<TaskDto> deleteTask(final UUID taskId) {
        return taskRepository.findById(taskId)
                .switchIfEmpty(Mono.error(new TaskMissingException()))
                .flatMap(task -> taskRepository.delete(task)
                        .thenReturn(task))
                .map(TaskDto::of);
    }
}
