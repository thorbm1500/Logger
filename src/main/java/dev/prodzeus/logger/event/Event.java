package dev.prodzeus.logger.event;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.event.exception.EventException;
import dev.prodzeus.logger.event.exception.ExceptionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Event {

    private static final ExecutorService threadPool;

    static {
        threadPool = Executors.newVirtualThreadPerTaskExecutor();
    }

    protected final Logger logger;
    protected final Collection<RegisteredListener> listeners = new HashSet<>();

    protected Event(final Logger logger) {
        if (logger == null) new ExceptionEvent(new EventException("Listener for event cannot be null!"));
        this.logger = logger;
    }

    protected boolean fireEvent(@NotNull final Event event) {
        listeners.addAll(EventManager.getHandlers(event));
        if (listeners.isEmpty()) return false;
        for (final RegisteredListener listener : listeners) {
            listener.accept(event);
        }
        return true;
    }

    protected synchronized boolean fireEventSync(@NotNull final Event event) {
        return fireEvent(event);
    }

    protected boolean fireEventAsync(@NotNull final Event event) {
        listeners.addAll(EventManager.getHandlers(event));
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

    public Collection<RegisteredListener> getRegisteredListeners() throws EventException {
        if (listeners.isEmpty()) throw new EventException(new IllegalStateException("No registered listeners found!"));
        return listeners;
    }
}
