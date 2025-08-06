package dev.prodzeus.logger.event;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.components.Level;
import dev.prodzeus.logger.event.components.EventListener;
import dev.prodzeus.logger.event.events.log.GenericLogEvent;
import dev.prodzeus.logger.slf4j.SLF4JProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Event {

    private static final ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor();

    protected final Logger logger;
    protected final Level level;

    protected Throwable throwable;

    private final String message;
    protected boolean hasFired = false;

    protected Event(@NotNull final Logger logger, @NotNull final Level level, @NotNull final String log) {
        this.logger = logger;
        this.level = level;
        this.message = GenericLogEvent.formatLogMessage(level,log,true);
        System.out.println("EVENT.");
    }

    protected void setThrowable(@NotNull final Throwable throwable) {
        this.throwable = throwable;
    }

    @Contract(pure = true)
    public boolean isException() {
        return throwable != null;
    }

    @Contract(pure = true)
    public @NotNull Throwable getCause() {
        return throwable.getCause();
    }

    @Contract(pure = true)
    public @NotNull String getExceptionMessage() {
        return throwable.getMessage();
    }

    @Contract(pure = true)
    public Level getLevel() {
        return level;
    }

    @Contract(pure = true)
    public String getMessage() {
        return message;
    }

    public boolean hasFired() {
        return hasFired;
    }

    protected @NotNull Collection<@NotNull EventListener> getListeners() {
        return SLF4JProvider.get().getEventManager().getListeners();
    }

    protected final void fireEvent(@NotNull final Runnable event) {
        if (hasFired) {
            return;
        }
        hasFired = true;
        event.run();
    }

    protected final synchronized void fireEventSync(@NotNull final Runnable event) {
        fireEvent(event);
    }

    protected final void fireEventAsync(@NotNull final Runnable event) {
        if (hasFired) return;
        hasFired = true;
        threadPool.submit(event);
    }

    public String getCaller() {
        return logger.getName();
    }

    protected abstract void fire();

    protected abstract void fireSynchronized();

    protected abstract void fireAsync();
}
