package dev.prodzeus.logger.event.events.log;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.components.Level;
import dev.prodzeus.logger.event.components.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;

import java.util.*;

public final class TraceLogEvent extends GenericLogEvent {

    public TraceLogEvent(@NotNull final Logger logger, @NotNull final Collection<Marker> marker, @NotNull final String log, @NotNull final Collection<Object> args) {
        super(logger, Level.TRACE, marker, log, args);
    }

    public TraceLogEvent(@NotNull final Logger logger, @NotNull final Collection<Marker> marker, @NotNull final String log) {
        this(logger, marker, log, Collections.emptySet());
    }

    public TraceLogEvent(@NotNull final Logger logger, @NotNull final String log, @NotNull final Collection<Object> args) {
        this(logger, Collections.emptySet(), log, args);
    }

    public TraceLogEvent(@NotNull final Logger logger, @NotNull final String log) {
        this(logger, Collections.emptySet(), log, Collections.emptySet());
    }

    @Override
    public void fire() {
        for (@NotNull final dev.prodzeus.logger.event.components.EventListener listener : getListeners()) {
            fireEvent(() -> {
                listener.onTraceLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }

    @Override
    public void fireSynchronized() {
        for (@NotNull final dev.prodzeus.logger.event.components.EventListener listener : getListeners()) {
            fireEventSync(() -> {
                listener.onTraceLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }

    @Override
    public void fireAsync() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventAsync(() -> {
                listener.onTraceLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }
}
