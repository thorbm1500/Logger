package dev.prodzeus.logger.event.exception;

import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ExceptionEvent extends Event {

    private final EventException exception;

    public ExceptionEvent(@NotNull final EventException exception) {
        super(SLF4JProvider.getSystem());
        this.exception = exception;
        if (!fireEvent(this)) {
            System.err.printf("Exception caught! Consider listening for `EventException`. %s \n", exception.getMessage());
            exception.printStackTrace();
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
        return exception.getTimestamp();
    }
}
