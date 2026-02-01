package dev.guilhermeluan.ongoing.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class BadRequestException extends ResponseStatusException {

    public BadRequestException(HttpStatusCode status, @Nullable String reason) {
        super(status, reason);
    }
}
