package dev.prodzeus.logger.internal;

import dev.prodzeus.logger.Event;
import dev.prodzeus.logger.Listener;
import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.components.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @apiNote <b>For internal use only.</b>
 */
public final class ListenerRegistry {

    private static final Logger LOGGER = SLF4JProvider.get().getLoggerFactory().getLogger(ListenerRegistry.class.getName());

    private static final Map<UUID,Listener> listeners = Collections.synchronizedMap(new HashMap<>());
    private static final HashMap<Level, HashMap<UUID,Consumer<Event>>> sinks = new HashMap<>();
    private static final Object mutex = new Object();

    public static void register(@NotNull final Listener listener) throws IllegalArgumentException {
        final Deque<Method> methods = new ArrayDeque<>(List.of(listener.getClass().getDeclaredMethods()));
        final Set<Level> levels = Arrays.stream(Level.values()).filter(lvl -> lvl.getWeight() > 0 && lvl.getWeight() < 1000).collect(Collectors.toCollection(HashSet::new));
        boolean isEmpty = true;
        while (!methods.isEmpty()) {
            final Method method = methods.poll();
            for (final Level level : levels) {
                if (!method.getDeclaringClass().equals(listener.getClass())
                    || !method.getName().toLowerCase().contains(level.toString().toLowerCase())) continue;
                synchronized (mutex) {
                    if (!sinks.containsKey(level)) {
                        sinks.put(level, new HashMap<>());
                    }
                    sinks.get(level).put(listener.getUniqueId(), event -> {
                        try {
                            method.setAccessible(true);
                            method.invoke(listener, event);
                        } catch (Exception e) {
                            LOGGER.exception(e);
                        }
                    });
                }
                isEmpty = false;
                break;
            }
        }

        if (isEmpty) throw new IllegalArgumentException("Cannot register new Listener with no methods defined!");
        listeners.put(listener.getUniqueId(),listener);
    }

    public static void unregister(@NotNull final Listener listener) {
        listeners.remove(listener.getUniqueId());
        synchronized (mutex) {
            sinks.values().forEach(map -> map.remove(listener.getUniqueId()));
        }
    }

    @Contract(pure = true)
    public static @NotNull Collection<Consumer<Event>> getListeners(@NotNull final Level level) {
        synchronized (mutex) {
            return Collections.unmodifiableCollection(sinks.getOrDefault(level, new HashMap<>()).values());
        }
    }
}
