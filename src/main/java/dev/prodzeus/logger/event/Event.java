package dev.prodzeus.logger.event;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.event.components.EventListener;
import dev.prodzeus.logger.event.components.RegisteredListener;
import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.events.exception.ExceptionEvent;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Event {

    private static final ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor();

    protected final Logger logger;
    protected final Collection<RegisteredListener> listeners = new HashSet<>();

    @SneakyThrows
    protected Event(final Logger logger) {
        if (logger == null) new ExceptionEvent(new EventException("Listener for event cannot be null!"));
        this.logger = logger;
    }

    @SneakyThrows
    protected boolean fireEvent(@NotNull final Event event) {
        listeners.addAll(SLF4JProvider.get().getEventManager().getHandlers(event));
        if (listeners.isEmpty()) return false;
        for (final RegisteredListener listener : listeners) {
            listener.accept(event);
        }
        return true;
    }

    protected boolean fireEventSync(@NotNull final Event event) {
        synchronized (Event.class) {
            return fireEvent(event);
        }
    }

    protected boolean fireEventAsync(@NotNull final Event event) {
        listeners.addAll(SLF4JProvider.get().getEventManager().getHandlers(event));
        if (listeners.isEmpty()) return false;
        for (final RegisteredListener listener : listeners) {
            threadPool.submit(() -> listener.accept(event));
        }
        return true;
    }

    public interface Executor {
        void execute(EventListener listener, Event event);
    }

    public String getCaller() {
        return logger.getName();
    }

    public Collection<RegisteredListener> getRegisteredListeners() {
        return listeners;
    }

    protected abstract void fire();
    protected abstract void fireSynchronized();
    protected abstract void fireAsync();
}
