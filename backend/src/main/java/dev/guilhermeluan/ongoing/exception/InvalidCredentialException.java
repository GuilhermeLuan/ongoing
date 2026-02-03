package dev.guilhermeluan.ongoing.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class InvalidCredentialException extends ResponseStatusException {

    public InvalidCredentialException(HttpStatusCode status, @Nullable String reason) {
        super(status, reason);
    }
}
