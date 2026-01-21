package dev.guilhermeluan.ongoing.subscriptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(Long id) {
        super("Subscription not found with id: " + id);
    }

    public SubscriptionNotFoundException(String message) {
        super(message);
    }
}
