package dev.prodzeus.logger.event.events.exception;

import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.events.log.GenericLogEvent;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ExceptionEvent extends GenericLogEvent {

    @SneakyThrows
    public ExceptionEvent(@NotNull final Throwable exception) {
        super(exception);
        if (!fireEventSync(this)) {
            System.out.flush();
            System.err.println("Exception caught! Consider listening for 'EventException'.");
            throw (Exception) getCause();
        }
    }

    @Contract(pure = true)
    public @NotNull Throwable getCause() {
        return exception.getCause();
    }

    @Contract(pure = true)
    public @NotNull String getMessage() {
        return exception.getMessage();
    }

    @Contract(pure = true)
    public @NotNull StackTraceElement[] getStackTrace() {
        return exception.getStackTrace();
    }

    @Contract(pure = true)
    public long getTimestamp() {
        if (exception instanceof EventException e) return e.getTimestamp();
        else return 0;
    }

    @Override
    protected void fire() {
        fireEvent(this);
    }

    @Override
    protected void fireSynchronized() {
        fireEventSync(this);
    }

    @Override
    protected void fireAsync() {
        fireEventAsync(this);
    }
}
