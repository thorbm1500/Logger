package dev.prodzeus.logger.event;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.event.components.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Event {

    private static final ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor();

    protected final Logger logger;
    protected boolean hasFired = false;

    protected Event(@NotNull final Logger logger) {
        this.logger = logger;
    }

    public boolean hasFired() {
        return hasFired;
    }

    protected @NotNull Collection<@NotNull EventListener> getListeners() {
        return SLF4JProvider.get().getEventManager().getListeners();
    }

    protected final void fireEvent(@NotNull final Runnable event) {
        if (hasFired) return;
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
