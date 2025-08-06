package dev.prodzeus.logger.event.events.exception;

import dev.prodzeus.logger.components.Level;
import dev.prodzeus.logger.event.Event;
import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.components.EventListener;
import dev.prodzeus.logger.slf4j.SLF4JProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ExceptionEvent extends Event {

    public ExceptionEvent(@NotNull final Throwable throwable) {
        super(SLF4JProvider.getSystem(), Level.EXCEPTION, throwable.getMessage());
        this.throwable = throwable;
        setThrowable(throwable);
        fire();
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
            fireEvent(() -> {
                listener.onExceptionEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }

    @Override
    public void fireSynchronized() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventSync(() -> {
                listener.onExceptionEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }

    @Override
    public void fireAsync() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventAsync(() -> {
                listener.onExceptionEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }
}
