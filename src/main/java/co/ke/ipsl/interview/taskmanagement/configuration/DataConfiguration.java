package co.ke.ipsl.interview.taskmanagement.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * @author Denis Gitonga
 */
@Configuration
@EnableR2dbcAuditing
@EnableR2dbcRepositories
public class DataConfiguration {
}
