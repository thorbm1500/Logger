package dev.prodzeus.logger.event.components;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.event.Event;
import dev.prodzeus.logger.event.events.exception.ExceptionEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class RegisteredListener {

    private final Logger logger;
    private final EventListener listener;
    private final Set<Event.Executor> executors;
    private final Set<Class<? extends Event>> events;

    private RegisteredListener(@NotNull final EventListener listener, @NotNull final Logger logger, @NotNull final Set<Class<? extends Event>> events, @NotNull final Set<Event.Executor> executors) {
        this.logger = logger;
        this.listener = listener;
        this.events = events;
        this.executors = executors;
    }

    public Logger getOwner() {
        return logger;
    }

    public EventListener getListenerRegistered() {
        return listener;
    }

    public Collection<Class<? extends Event>> getEventsListeningFor() {
        return events;
    }

    public void accept(@NotNull final Event event) {
        for (final Event.Executor executor : executors) {
            executor.execute(listener,event);
        }
    }

    public static @NotNull RegisteredListener createNewListener(@NotNull final EventListener listener, @NotNull final Logger logger) throws Exception {
        SLF4JProvider.getSystem().traceAsync("Attempting new registration of {} for logger instance {}",listener.getClass().getSimpleName(),logger.getName());
        final HashSet<Method> methods = HashSet.newHashSet(4);
        try {
            final Method[] definedMethods = listener.getClass().getMethods();
            for (final Method method : definedMethods) {
                methods.add(method);
            }
            for (final Method method : listener.getClass().getDeclaredMethods()) {
                methods.add(method);
            }
        } catch (Exception e) {
            new ExceptionEvent(e);
        }

        if (methods.isEmpty()) new ExceptionEvent(new IllegalStateException("No methods found!"));

        final HashSet<Class<? extends Event>> events = HashSet.newHashSet(4);
        final HashSet<Event.Executor> executors = HashSet.newHashSet(4);
        for (final Method method : methods) {
            if (method == null) {
                new ExceptionEvent(new IllegalStateException("Method is null!"));
                continue;
            }

            final EventHandler eventHandler = method.getAnnotation(EventHandler.class);
            if (eventHandler == null) continue;

            if (method.getParameterCount() != 1) {
                new ExceptionEvent(new IllegalStateException("Method %s must have exactly one parameter!".formatted(method.getName())));
                continue;
            }

            final Class<?> clazz;
            if (!Event.class.isAssignableFrom(clazz = method.getParameterTypes()[0])) {
                new ExceptionEvent(new IllegalStateException("Method %s must implement a valid Event!".formatted(method.getName())));
                continue;
            }
            method.setAccessible(true);
            final Class<? extends Event> event = clazz.asSubclass(Event.class);

            events.add(event);
            executors.add((l, e) -> {
                try {
                    if (event.isAssignableFrom(e.getClass())) method.invoke(l, e);
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                } catch (Throwable t) {
                    System.out.println("Log failed!");
                    t.printStackTrace();
                }
            });

        }

        return new RegisteredListener(listener, logger,events,executors);
    }
}
