package co.ke.ipsl.interview.taskmanagement.task;

import co.ke.ipsl.interview.taskmanagement.task.dto.CreateTaskDto;
import co.ke.ipsl.interview.taskmanagement.task.dto.TaskDto;
import co.ke.ipsl.interview.taskmanagement.task.dto.UpdateTaskDto;
import co.ke.ipsl.interview.taskmanagement.task.model.Task;
import co.ke.ipsl.interview.taskmanagement.task.model.TaskRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * @author Denis Gitonga
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskServiceIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TaskRepository taskRepository;

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
            .withPassword("s3cr3t")
            .withUsername("postgres")
            .withDatabaseName("tasks");

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redisContainer =
            new GenericContainer<>(DockerImageName.parse("redis:latest"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        var options = PostgreSQLR2DBCDatabaseContainer.getOptions(postgreSQLContainer);

        // R2DBC
        registry.add("spring.r2dbc.url", () ->
                format("r2dbc:postgresql://%s:%d/%s",
                        options.getValue(HOST),
                        options.getValue(PORT),
                        options.getValue(DATABASE)));
        registry.add("spring.r2dbc.username", () -> options.getValue(USER));
        registry.add("spring.r2dbc.password", () -> options.getValue(PASSWORD));

        // LIQUIBASE
        registry.add("spring.liquibase.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.liquibase.user", () -> options.getValue(USER));
        registry.add("spring.liquibase.password", () -> options.getValue(PASSWORD));

        // REDIS
        registry.add("spring.data.redis.host", () -> redisContainer.getHost());
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @BeforeEach
    void setup() {
        taskRepository.deleteAll()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    @DisplayName("should create task")
    void createTask() {
        final var faker = new Faker();
        final var createTaskDto = new CreateTaskDto(
                faker.text().text(5, 100),
                faker.text().text(1000),
                faker.date().future(2, TimeUnit.DAYS).toLocalDateTime()
        );

        webTestClient.post()
                .uri("/tasks")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(createTaskDto)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(TaskDto.class);

        taskRepository.findByTitle(createTaskDto.title())
                .as(StepVerifier::create)
                .assertNext(task -> {
                    assertThat(task).isNotNull();
                    assertThat(task.createdAt()).isNotNull();
                    assertThat(task.updatedAt()).isNotNull();
                    assertThat(task.id()).isNotNull();
                    assertThat(task.version()).isEqualTo(1);

                    assertThat(task.title()).isNotNull()
                            .isEqualTo(createTaskDto.title());
                    assertThat(task.description()).isNotNull()
                            .isEqualTo(createTaskDto.description());
                    assertThat(task.dueDate()).isNotNull()
                            .isEqualTo(createTaskDto.dueDate());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should update task")
    void updateTask() {
        final var faker = new Faker();
        final var createTaskDto = new CreateTaskDto(
                faker.text().text(5, 100),
                faker.text().text(1000),
                faker.date().future(2, TimeUnit.DAYS).toLocalDateTime()
        );

        Task existingTask = taskRepository.save(Task.of(createTaskDto)).block();

        final var updateTaskDto = new UpdateTaskDto(
                faker.text().text(5, 100),
                faker.text().text(1000),
                faker.date().future(2, TimeUnit.DAYS).toLocalDateTime()
        );

        assertThat(existingTask).isNotNull()
                .extracting(Task::id)
                .isNotNull();

        webTestClient.put()
                .uri("/tasks/{taskId}", existingTask.id())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(updateTaskDto)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TaskDto.class);

        taskRepository.findById(existingTask.id())
                .as(StepVerifier::create)
                .assertNext(task -> {
                    assertThat(task).isNotNull();
                    assertThat(task.createdAt())
                            .isNotNull()
                            .isEqualTo(existingTask.createdAt());
                    assertThat(task.updatedAt())
                            .isNotNull()
                            .isNotEqualTo(existingTask.updatedAt());
                    assertThat(task.id()).isNotNull();
                    assertThat(task.version())
                            .isGreaterThan(existingTask.version());

                    assertThat(task.title()).isNotNull()
                            .isEqualTo(updateTaskDto.title())
                            .isNotEqualTo(createTaskDto.title());
                    assertThat(task.description()).isNotNull()
                            .isEqualTo(updateTaskDto.description())
                            .isNotEqualTo(createTaskDto.description());
                    assertThat(task.dueDate()).isNotNull()
                            .isEqualTo(updateTaskDto.dueDate())
                            .isNotEqualTo(createTaskDto.dueDate());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should fetch all tasks")
    void fetchAllTasks() {

        final var faker = new Faker();
        final var tasks = IntStream.range(0, 5)
                .mapToObj(value -> new CreateTaskDto(
                        faker.text().text(5, 100),
                        faker.text().text(1000),
                        faker.date().future(2, TimeUnit.DAYS).toLocalDateTime()
                ))
                .map(Task::of)
                .toList();
        taskRepository.saveAll(tasks).collectList()
                .as(StepVerifier::create)
                .assertNext(savedTasks -> assertThat(savedTasks).hasSize(tasks.size()))
                .verifyComplete();

        webTestClient.get()
                .uri("/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalElements").isEqualTo(tasks.size())
                .jsonPath("$.content").isArray()
                .jsonPath("$.pageable.pageSize").isEqualTo(20)
                .jsonPath("$.pageable.pageNumber").isEqualTo(0);
    }

    @Test
    @DisplayName("should delete task")
    void deleteTask() {
        final var faker = new Faker();
        final var createTaskDto = new CreateTaskDto(
                faker.text().text(5, 100),
                faker.text().text(1000),
                faker.date().future(2, TimeUnit.DAYS).toLocalDateTime()
        );

        Task existingTask = taskRepository.save(Task.of(createTaskDto)).block();

        assertThat(existingTask).isNotNull()
                .extracting(Task::id)
                .isNotNull();

        webTestClient.delete()
                .uri("/tasks/{taskId}", existingTask.id())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TaskDto.class);

        taskRepository.findAll()
                .collectList()
                .as(StepVerifier::create)
                .assertNext(tasks -> assertThat(tasks).isEmpty())
                .verifyComplete();
    }
}