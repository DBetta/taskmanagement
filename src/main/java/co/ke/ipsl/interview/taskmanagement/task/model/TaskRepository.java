package co.ke.ipsl.interview.taskmanagement.task.model;

import co.ke.ipsl.interview.taskmanagement.task.dto.FilterTaskDto;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author Denis Gitonga
 */
public interface TaskRepository extends R2dbcRepository<Task, UUID> {

    Flux<Task> findAllBy(Pageable pageable, Example<FilterTaskDto> example);

    Mono<Task> findByTitle(String title);

}
