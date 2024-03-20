package co.ke.ipsl.interview.taskmanagement.security;

import java.util.UUID;

/**
 * @author Denis Gitonga
 */
public record UserProjection(
        UUID id,

        String username,

        String password,

        String firstName,

        String lastName
) {
}
