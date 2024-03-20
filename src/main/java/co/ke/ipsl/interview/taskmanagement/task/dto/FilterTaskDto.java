package co.ke.ipsl.interview.taskmanagement.task.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.relational.core.query.Criteria;

import java.time.LocalDateTime;

import static org.springframework.data.relational.core.query.Criteria.from;

/**
 * @author Denis Gitonga
 */
public record FilterTaskDto(
        @Nullable
        String title,

        @Nullable
        LocalDateTime dueDate
) {
    @NotNull
    public Criteria criteria() {
        var criteria = from();
        if (title() != null) {
            criteria = criteria.and("title").is(title());
        }

        if (dueDate() != null) {
            criteria = criteria.and("dueDate").is(dueDate());
        }

        return criteria;
    }
}
