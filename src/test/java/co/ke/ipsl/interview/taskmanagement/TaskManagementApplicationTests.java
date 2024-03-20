package co.ke.ipsl.interview.taskmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;
import static java.lang.String.format;

@Testcontainers
@SpringBootTest
class TaskManagementApplicationTests {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
            .withPassword("s3cr3t")
            .withUsername("postgres")
            .withDatabaseName("tasks");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        var options = PostgreSQLR2DBCDatabaseContainer.getOptions(container);

        // R2DBC
        registry.add("spring.r2dbc.url", () ->
                format("r2dbc:postgresql://%s:%d/%s",
                        options.getValue(HOST),
                        options.getValue(PORT),
                        options.getValue(DATABASE)));
        registry.add("spring.r2dbc.username", () -> options.getValue(USER));
        registry.add("spring.r2dbc.password", () -> options.getValue(PASSWORD));

        // LIQUIBASE
        registry.add("spring.liquibase.url", container::getJdbcUrl);
        registry.add("spring.liquibase.user", () -> options.getValue(USER));
        registry.add("spring.liquibase.password", () -> options.getValue(PASSWORD));
    }

    @Test
    void contextLoads() {
    }

}
