package dev.prodzeus.logger.event.components;

import dev.prodzeus.logger.event.events.exception.ExceptionEvent;
import lombok.SneakyThrows;

import java.io.Serial;

public final class EventException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;
    private final Throwable cause;
    private final long timestamp;

    @SneakyThrows
    public EventException(final String message, final Throwable cause) {
        super(message);
        this.timestamp = System.currentTimeMillis();
        this.cause = cause;
        new ExceptionEvent(this);
    }

    @SneakyThrows
    public EventException(final Throwable cause) {
        super();
        this.timestamp = System.currentTimeMillis();
        this.cause = cause;
        new ExceptionEvent(this);
    }

    @SneakyThrows
    public EventException(final String message) {
        super(message);
        this.timestamp = System.currentTimeMillis();
        this.cause = null;
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
