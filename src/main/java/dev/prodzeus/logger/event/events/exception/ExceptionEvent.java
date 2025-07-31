package dev.prodzeus.logger.event.events.exception;

import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.event.Event;
import dev.prodzeus.logger.event.components.EventException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ExceptionEvent extends Event {

    private final EventException exception;

    public ExceptionEvent(@NotNull final EventException exception) throws Exception {
        super(SLF4JProvider.getSystem());
        this.exception = exception;
        if (!fireEvent(this)) {
            System.out.flush();
            System.err.println("Exception caught! Consider listening for 'EventException'.");
            throw exception;
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

    @Override
    protected void fire() {
        /* Unused */
        fireEvent(this);
    }

    @Override
    protected void fireSynchronized() {
        /* Unused */
        fireEventSync(this);
    }

    @Override
    protected void fireAsync() {
        /* Unused */
        fireEventAsync(this);
    }
}
