package co.ke.ipsl.interview.taskmanagement.task;

import co.ke.ipsl.interview.taskmanagement.configuration.SecurityConfiguration;
import co.ke.ipsl.interview.taskmanagement.task.dto.CreateTaskDto;
import co.ke.ipsl.interview.taskmanagement.task.dto.TaskDto;
import co.ke.ipsl.interview.taskmanagement.task.model.Task;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ProblemDetail;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * @author Denis Gitonga
 */
@WebFluxTest(controllers = TaskController.class,
        excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
class TaskControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TaskService taskService;


    @Test
    @DisplayName("should create task")
    void createTask() {

        final var faker = new Faker();
        final var createTaskDto = new CreateTaskDto(
                faker.text().text(5, 100),
                faker.text().text(1000),
                faker.date().future(2, TimeUnit.DAYS).toLocalDateTime()
        );

        given(taskService.createTask(createTaskDto)).willAnswer(invocation -> {
            var task = Task.of(createTaskDto).withId(UUID.randomUUID());
            var taskDto = TaskDto.of(task);
            return Mono.just(taskDto);
        });


        webTestClient.post()
                .uri("/tasks")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(createTaskDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(TaskDto.class)
                .value(TaskDto::description, equalTo(createTaskDto.description()))
                .value(TaskDto::title, equalTo(createTaskDto.title()))
                .value(TaskDto::dueDate, equalTo(createTaskDto.dueDate()))
                .value(TaskDto::id, notNullValue());

        verify(taskService).createTask(createTaskDto);

    }

    @Test
    @DisplayName("should provide all task parameters")
    void createTask_all_parameters() {
        webTestClient.post()
                .uri("/tasks")
                .bodyValue(new CreateTaskDto(null, null, null))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ProblemDetail.class);


    }


    @Test
    @DisplayName("should provide title length")
    void createTask_valid_title_length() {
        final var faker = new Faker();

        webTestClient.post()
                .uri("/tasks")
                .bodyValue(new CreateTaskDto(
                        faker.text().text(4),
                        faker.text().text(1000),
                        faker.date().future(2, TimeUnit.DAYS).toLocalDateTime()
                ))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ProblemDetail.class);


        webTestClient.post()
                .uri("/tasks")
                .bodyValue(new CreateTaskDto(
                        faker.text().text(101),
                        faker.text().text(1000),
                        faker.date().future(2, TimeUnit.DAYS).toLocalDateTime()
                ))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ProblemDetail.class);
    }

    @Test
    @DisplayName("should create task with due date in future")
    void createTask_future_due_date() {
        final var faker = new Faker();

        webTestClient.post()
                .uri("/tasks")
                .bodyValue(new CreateTaskDto(
                        faker.text().text(5),
                        faker.text().text(1000),
                        faker.date().past(1, TimeUnit.DAYS).toLocalDateTime()
                ))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ProblemDetail.class);
    }


}