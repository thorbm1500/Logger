package dev.prodzeus.logger.event;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.components.EventListener;
import dev.prodzeus.logger.event.components.RegisteredListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class EventManager {

    private final Set<RegisteredListener> registeredListeners = new HashSet<>();

    public EventManager() {
    }

    @Contract(pure = true)
    public @NotNull Collection<RegisteredListener> getHandlers(@NotNull final Event event) {
        return getHandlers(event.getClass());
    }


    @Contract(pure = true)
    public @NotNull Collection<RegisteredListener> getHandlers(@NotNull final Class<? extends Event> event) {
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
    public synchronized @NotNull Collection<RegisteredListener> getAllHandlers() {
        return registeredListeners;
    }

    public RegisteredListener registerListener(@NotNull final EventListener listener, @NotNull final Logger logger) throws Exception {
        synchronized (registeredListeners) {
            if (!registeredListeners.isEmpty()) {
                for (final RegisteredListener l : registeredListeners) {
                    if (l == null) continue;

                    final Logger owner = l.getOwner();
                    if (listener.getClass().equals(SLF4JProvider.DefaultListener.class)
                        || owner.getName().equals(logger.getName())
                           && l.getListenerRegistered().getClass().isInstance(listener.getClass())) return null;
                }
            }
            final RegisteredListener newListener = RegisteredListener.createNewListener(listener, logger);
            registeredListeners.add(newListener);
            return newListener;
        }
    }

    public boolean unregisterListener(@NotNull final EventListener listener, @NotNull final Logger logger) {
        synchronized (registeredListeners) {
            if (registeredListeners.isEmpty()) return true;
            return registeredListeners.removeIf(
                    registeredListener -> registeredListener.getListenerRegistered().equals(listener)
                                          && registeredListener.getOwner().equals(logger));
        }
    }

    public void unregisterAll(@NotNull final Logger logger) throws EventException {
        synchronized (registeredListeners) {
            try {
                registeredListeners.removeIf(registeredListener -> registeredListener.getOwner().equals(logger));
            } catch (Exception e) {
                throw new EventException(new RuntimeException(e));
            }
        }
    }
}