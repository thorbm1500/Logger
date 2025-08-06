package dev.prodzeus.logger.event.events.log;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.components.Level;
import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.components.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public final class ExceptionLogEvent extends GenericLogEvent {

    public ExceptionLogEvent(@NotNull final Logger logger, @NotNull final Collection<Marker> markers, @NotNull final String log, @NotNull final Collection<Object> args) {
        super(logger, Level.EXCEPTION, markers, log, args);
    }

    public ExceptionLogEvent(@NotNull final Logger logger, @NotNull final Collection<Marker> marker, @NotNull final String log) {
        this(logger, marker, log, Collections.emptySet());
    }

    public ExceptionLogEvent(@NotNull final Logger logger, @NotNull final String log, @NotNull final Collection<Object> args) {
        this(logger, Collections.emptySet(), log, args);
    }

    public ExceptionLogEvent(@NotNull final Logger logger, @NotNull final String log) {
        this(logger, Collections.emptySet(), log, Collections.emptySet());
    }

    public ExceptionLogEvent(@NotNull final String log) {
        super(new EventException(log));
    }

    public ExceptionLogEvent(@NotNull final Throwable cause) {
        super(cause);
    }

    @Override
    public void fire() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEvent(() -> {
                listener.onExceptionLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }

    @Override
    public void fireSynchronized() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventSync(() -> {
                listener.onExceptionLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }

    @Override
    public void fireAsync() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventAsync(() -> {
                listener.onExceptionLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }
}
