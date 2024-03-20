package co.ke.ipsl.interview.taskmanagement.security;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * @author Denis Gitonga
 */
@Component
public class UserDetailsService implements ReactiveUserDetailsService {

    private final DatabaseClient client;

    public UserDetailsService(DatabaseClient client) {
        this.client = client;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        return client.sql("""
                        select id, username, password, email, first_name, last_name from tasks.users
                        where email = :email
                        """)
                .bind("email", username)
                .map((row, rowMetadata) -> new UserProjection(
                        row.get("id", UUID.class),
                        row.get("email", String.class),
                        row.get("password", String.class),
                        row.get("first_name", String.class),
                        row.get("last_name", String.class)
                ))
                .one()
                .flatMap(userProjection -> fetchUserRoles(userProjection.id())
                        .map(userRoles -> User.withUsername(userProjection.username())
                                .password(userProjection.password())
                                .disabled(false)
                                .accountExpired(false)
                                .build()
                        ));
    }

    Mono<List<String>> fetchUserRoles(UUID userId) {
        return client.sql("""
                        select role from tasks.roles
                        join tasks.user_roles ur on roles.id = ur.rol_id
                        where usr_id = :user_id
                        """)
                .bind("user_id", userId)
                .mapValue(String.class)
                .all()
                .collectList();
    }
}
