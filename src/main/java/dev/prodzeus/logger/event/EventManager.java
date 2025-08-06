package dev.prodzeus.logger.event;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.event.components.EventListener;
import dev.prodzeus.logger.event.events.log.ExceptionLogEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class EventManager {

    private static final HashSet<@NotNull EventListener> registeredListeners = new HashSet<>();

    public EventManager() {}

    /**
     * Get all listeners registered.
     * @return An unmodifiable {@link Set}, that's either empty,
     * or containing <b>@NotNull</b> EventListeners.
     */
    public Set<@NotNull EventListener> getListeners() {
        return new HashSet<>(registeredListeners);
    }

    public boolean registerListener(@NotNull final EventListener listener) {
        synchronized (registeredListeners) {
            if (!registeredListeners.isEmpty()) {
                for (final EventListener l : registeredListeners) {
                    if (l.equals(listener)) return false;
                }
            }
            return registeredListeners.add(listener);
        }
    }

    public boolean unregisterListener(@NotNull final EventListener listener) {
        if (registeredListeners.isEmpty()) return true;
        synchronized (registeredListeners) {
            return registeredListeners.removeIf(registeredListener -> registeredListener.equals(listener));
        }
    }

    public void unregisterAll(@NotNull final Logger logger) {
        try {
            synchronized (registeredListeners) {
                registeredListeners.removeIf(registeredListener -> registeredListener.getOwner().equals(logger));
            }
        } catch (Exception e) {
            new ExceptionLogEvent(new RuntimeException(e));
        }
    }

    public void unregisterAll() {
        try {
            synchronized (registeredListeners) {
                registeredListeners.clear();
            }
        } catch (Exception e) {
            new ExceptionLogEvent(new RuntimeException(e));
        }
    }
}