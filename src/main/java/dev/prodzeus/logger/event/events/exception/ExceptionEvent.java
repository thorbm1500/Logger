package dev.prodzeus.logger.event.events.exception;

import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.event.Event;
import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.components.EventListener;
import dev.prodzeus.logger.event.events.log.ExceptionLogEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class ExceptionEvent extends Event {

    private final Throwable throwable;

    public ExceptionEvent(@NotNull final Throwable throwable) {
        super(SLF4JProvider.getSystem());
        this.throwable = throwable;
        if (getListeners()
                .stream()
                .noneMatch(
                        l -> Arrays
                                .stream(l.getClass()
                                        .getDeclaredMethods())
                                .noneMatch(
                                        m -> m.getName().equalsIgnoreCase("onExceptionEvent")))) {
            System.out.flush();
            SLF4JProvider.getSystem().warn("Uncaught exception thrown! Consider listening for 'EventException'.");
            new ExceptionLogEvent(throwable).fireSynchronized();
        }
    }

    @Contract(pure = true)
    public @NotNull Throwable getCause() {
        return throwable.getCause();
    }

    @Contract(pure = true)
    public @NotNull String getMessage() {
        return throwable.getMessage();
    }

    @Contract(pure = true)
    public @NotNull StackTraceElement[] getStackTrace() {
        return throwable.getStackTrace();
    }

    @Contract(pure = true)
    public long getTimestamp() {
        if (throwable instanceof EventException e) return e.getTimestamp();
        else return 0;
    }

    @Override
    public void fire() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEvent(() -> listener.onExceptionEvent(this));
        }
    }

    @Override
    public void fireSynchronized() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventSync(() -> listener.onExceptionEvent(this));
        }
    }

    @Override
    public void fireAsync() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventAsync(() -> listener.onExceptionEvent(this));
        }
    }
}
