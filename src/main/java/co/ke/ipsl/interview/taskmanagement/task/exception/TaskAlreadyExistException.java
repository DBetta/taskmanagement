package co.ke.ipsl.interview.taskmanagement.task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Denis Gitonga
 */
@ResponseStatus(code = HttpStatus.CONFLICT)
public class TaskAlreadyExistException extends RuntimeException {
}
