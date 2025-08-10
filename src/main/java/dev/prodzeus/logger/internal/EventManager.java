package dev.prodzeus.logger.internal;

import dev.prodzeus.logger.Event;
import dev.prodzeus.logger.Listener;
import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.SLF4JProvider;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * <b>For internal use only.</b>
 */
public final class EventManager {

    private EventManager() {}

    private static final Logger LOGGER = SLF4JProvider.get().getLoggerFactory().getLogger("dev.prodzeus.logger.internal.EventManager");

    private static final Map<UUID, Listener> listeners = new ConcurrentHashMap<>();

    public static void handleEvent(@NotNull final Event event) {
        final Collection<Consumer<Event>> eventHandlers = ListenerRegistry.getListeners(event.getLevel());
        if (!eventHandlers.isEmpty()) {
            EventScheduler.addToBatch(() -> {
                for (@NotNull final Consumer<Event> consumer : eventHandlers) {
                    try {
                        consumer.accept(event);
                    } catch (Exception e) {
                        LOGGER.exception(e);
                    }
                }
            });
        }
    }
}
