package dev.prodzeus.logger.event;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.event.components.EventListener;
import dev.prodzeus.logger.event.components.RegisteredListener;
import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.events.exception.ExceptionEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class EventManager {

    private static EventManager instance;

    private static final Set<RegisteredListener> registeredListeners;

    static {
        registeredListeners = new HashSet<>();
    }

    private EventManager() {
        instance = this;
    }

    public static synchronized EventManager getInstance() {
        return instance == null ? new EventManager() : instance;
    }

    @Contract(pure = true)
    public static @NotNull Collection<RegisteredListener> getHandlers(@NotNull final Event event) {
        return getHandlers(event.getClass());
    }


    @Contract(pure = true)
    public static @NotNull Collection<RegisteredListener> getHandlers(@NotNull final Class<? extends Event> event) {
        final Collection<RegisteredListener> handlers = new HashSet<>();

        synchronized (registeredListeners) {
            for (final RegisteredListener listener : registeredListeners) {
                for (final Class<? extends Event> clazz : listener.getEventsListeningFor()) {
                    if (clazz.isAssignableFrom(event)) handlers.add(listener);
                }
            }
        }

        return handlers;
    }

    @Contract(pure = true)
    public static synchronized @NotNull Collection<RegisteredListener> getAllHandlers() {
        return registeredListeners;
    }

    public static boolean registerListener(@NotNull final EventListener listener, @NotNull final Logger logger) throws EventException {
        synchronized (registeredListeners) {
            if (!registeredListeners.isEmpty()) {
                for (final RegisteredListener l : registeredListeners) {
                    if (l != null && l.getListenerRegistered().equals(listener)) return true;
                }
            }

            try {
                return registeredListeners.add(RegisteredListener.createNewListener(listener,logger));
            } catch (final Exception e) {
                new ExceptionEvent(e);
                return false;
            }
        }
    }

    public static boolean unregisterListener(@NotNull final EventListener listener, @NotNull final Logger logger) {
        synchronized (registeredListeners) {
            if (registeredListeners.isEmpty()) return true;
            return registeredListeners.removeIf(
                    registeredListener -> registeredListener.getListenerRegistered().equals(listener)
                                          && registeredListener.getOwner().equals(logger));
        }
    }

    public static void unregisterAll(@NotNull final Logger logger) throws EventException {
        synchronized (registeredListeners) {
            try {
                registeredListeners.removeIf(registeredListener -> registeredListener.getOwner().equals(logger));
            } catch (Exception e) {
                throw new EventException(new RuntimeException(e));
            }
        }
    }
}