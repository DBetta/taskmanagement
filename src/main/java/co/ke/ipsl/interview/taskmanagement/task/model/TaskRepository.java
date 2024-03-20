package co.ke.ipsl.interview.taskmanagement.task.model;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author Denis Gitonga
 */
public interface TaskRepository extends R2dbcRepository<Task, UUID> {

    Mono<Task> findByTitle(String title);

}
