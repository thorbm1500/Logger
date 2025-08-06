package dev.prodzeus.logger.event.events.log;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.components.Level;
import dev.prodzeus.logger.event.components.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;

import java.util.Collection;
import java.util.Collections;

public final class LogEvent extends GenericLogEvent {


    public LogEvent(@NotNull Logger logger, @NotNull Collection<Marker> marker, @NotNull String log, @NotNull Collection<Object> args) {
        super(logger, Level.ALL, marker, log, args);
        fireSynchronized();
    }

    public LogEvent(@NotNull final Logger logger, @NotNull final Collection<Marker> marker, @NotNull final String log) {
        this(logger, marker, log, Collections.emptySet());
    }

    public LogEvent(@NotNull final Logger logger, @NotNull final String log, @NotNull final Collection<Object> args) {
        this(logger, Collections.emptySet(), log, args);
    }

    public LogEvent(@NotNull final Logger logger, @NotNull final String log) {
        this(logger, Collections.emptySet(), log, Collections.emptySet());
    }

    @Override
    protected void fire() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEvent(() -> {
                listener.onLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }

    @Override
    protected void fireSynchronized() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventSync(() -> {
                listener.onLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }

    @Override
    protected void fireAsync() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventAsync(() -> {
                listener.onLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }
}
