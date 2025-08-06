package dev.prodzeus.logger.event.components;

import dev.prodzeus.logger.event.events.exception.ExceptionEvent;
import lombok.SneakyThrows;

import java.io.Serial;

public final class EventException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Throwable cause = this;
    private final long timestamp = System.currentTimeMillis();

    @SneakyThrows
    public EventException(final String message, final Throwable cause) {
        super(message,cause);
        new ExceptionEvent(this);
    }

    @SneakyThrows
    public EventException(final Throwable cause) {
        super(cause);
        new ExceptionEvent(this);
    }

    @SneakyThrows
    public EventException(final String message) {
        super(message);
        new ExceptionEvent(this);
    }

    @Override
    public synchronized Throwable getCause() {
        return cause;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
