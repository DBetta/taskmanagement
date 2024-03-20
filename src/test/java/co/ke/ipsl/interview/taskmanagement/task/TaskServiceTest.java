package co.ke.ipsl.interview.taskmanagement.task;

import co.ke.ipsl.interview.taskmanagement.task.dto.CreateTaskDto;
import co.ke.ipsl.interview.taskmanagement.task.dto.UpdateTaskDto;
import co.ke.ipsl.interview.taskmanagement.task.exception.TaskAlreadyExistException;
import co.ke.ipsl.interview.taskmanagement.task.exception.TaskMissingException;
import co.ke.ipsl.interview.taskmanagement.task.model.Task;
import co.ke.ipsl.interview.taskmanagement.task.model.TaskRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * @author Denis Gitonga
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private final Faker faker = new Faker();

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    //<editor-fold desc="create task tests">
    @Test
    @DisplayName("should create task successfully")
    void createTask() {

        var taskId = UUID.randomUUID();
        given(taskRepository.findByTitle(any())).willReturn(Mono.empty());
        given(taskRepository.save(any())).willAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            return Mono.just(task.withId(taskId));
        });

        var createTaskDto = new CreateTaskDto(
                faker.text().text(5, 100),
                faker.text().text(1000),
                faker.date().future(2, TimeUnit.DAYS).toLocalDateTime()
        );
        taskService.createTask(createTaskDto)
                .as(StepVerifier::create)
                .assertNext(taskDto -> {
                    assertThat(taskDto).isNotNull();
                    assertThat(taskDto.id()).isNotNull().isEqualTo(taskId);
                    assertThat(taskDto.dueDate()).isEqualTo(createTaskDto.dueDate());
                    assertThat(taskDto.description()).isEqualTo(createTaskDto.description());
                    assertThat(taskDto.title()).isEqualTo(createTaskDto.title());
                })
                .verifyComplete();

        verify(taskRepository).findByTitle(createTaskDto.title());
        verify(taskRepository).save(any());
    }

    @Test
    @DisplayName("should not create task when title is duplicate")
    void createTask_ExistingTitle() {

        given(taskRepository.findByTitle(any())).willReturn(Mono.just(mock()));
        taskService.createTask(mock())
                .as(StepVerifier::create)
                .verifyError(TaskAlreadyExistException.class);

        verify(taskRepository).findByTitle(any());
        verify(taskRepository, times(0)).save(any());
    }
    //</editor-fold>

    //<editor-fold desc="update task tests">
    @Test
    @DisplayName("should update task successfully")
    void updateTask() {
        var taskId = UUID.randomUUID();
        final var createTaskDto = new CreateTaskDto(
                faker.text().text(5, 100),
                faker.text().text(1000),
                faker.date().future(2, TimeUnit.DAYS).toLocalDateTime()
        );
        final var task = Task.of(createTaskDto).withId(taskId);
        given(taskRepository.findById(taskId)).willReturn(Mono.just(task));
        given(taskRepository.save(any())).willAnswer(invocation -> {
            Task updatedTask = invocation.getArgument(0);
            return Mono.just(updatedTask);
        });

        final var updateTaskDto = new UpdateTaskDto(
                faker.text().text(5, 100),
                faker.text().text(1000),
                faker.date().future(2, TimeUnit.DAYS).toLocalDateTime()
        );

        taskService.updateTask(taskId, updateTaskDto)
                .as(StepVerifier::create)
                .assertNext(taskDto -> {

                    assertThat(taskDto).isNotNull();
                    assertThat(taskDto.dueDate())
                            .isNotNull()
                            .isEqualTo(updateTaskDto.dueDate())
                            .isNotEqualTo(createTaskDto.dueDate());
                    assertThat(taskDto.title())
                            .isNotNull()
                            .isEqualTo(updateTaskDto.title())
                            .isNotEqualTo(createTaskDto.title());
                    assertThat(taskDto.description())
                            .isNotNull()
                            .isEqualTo(updateTaskDto.description())
                            .isNotEqualTo(createTaskDto.description());
                })
                .verifyComplete();

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any());
    }

    @Test
    @DisplayName("should not update task if taskId missing")
    void updateTask_NoId() {

        final var taskId = UUID.randomUUID();
        final var updateTaskDto = new UpdateTaskDto(
                faker.text().text(5, 100),
                faker.text().text(1000),
                faker.date().future(2, TimeUnit.DAYS).toLocalDateTime()
        );

        given(taskRepository.findById(taskId)).willReturn(Mono.empty());

        taskService.updateTask(taskId, updateTaskDto)
                .as(StepVerifier::create)
                .verifyError(TaskMissingException.class);

        verify(taskRepository).findById(taskId);
        verify(taskRepository, times(0)).save(any());
    }
    //</editor-fold>

    //<editor-fold desc="delete task tests">
    @Test
    @DisplayName("should delete task successfully")
    void deleteTask() {
        final var taskId = UUID.randomUUID();
        final Task task = mock();

        given(taskRepository.findById(taskId)).willReturn(Mono.just(task));

        given(taskRepository.delete(task)).willAnswer(invocation -> {
            Task toDeleteTask = invocation.getArgument(0);

            return Mono.just(toDeleteTask);
        });

        taskService.deleteTask(taskId)
                .as(StepVerifier::create)
                .assertNext(taskDto -> assertThat(taskDto).isNotNull())
                .verifyComplete();

        verify(taskRepository).findById(taskId);
        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("should not delete task if taskId is missing")
    void deleteTask_ErrorOnMissingTaskId() {

        given(taskRepository.findById(Mockito.<UUID>any())).willReturn(Mono.empty());

        taskService.deleteTask(mock())
                .as(StepVerifier::create)
                .verifyError(TaskMissingException.class);

        verify(taskRepository).findById(Mockito.<UUID>any());
        verify(taskRepository, times(0)).delete(any());

    }
    //</editor-fold>

}