package dev.prodzeus.logger.event.exception;

import java.io.Serial;

public class EventException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;
    private final Throwable cause;
    private final long timestamp;

    public EventException(final String message, final Throwable cause) {
        super(message);
        this.timestamp = System.currentTimeMillis();
        this.cause = cause;
        new ExceptionEvent(this);
    }

    public EventException(final Throwable cause) {
        super();
        this.timestamp = System.currentTimeMillis();
        this.cause = cause;
        new ExceptionEvent(this);
    }

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
