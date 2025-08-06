package dev.prodzeus.logger.event.events.log;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.components.Level;
import dev.prodzeus.logger.event.components.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;

import java.util.Collection;
import java.util.Collections;

public final class InfoLogEvent extends GenericLogEvent {

    public InfoLogEvent(@NotNull final Logger logger, @NotNull final Collection<Marker> marker, @NotNull final String log, @NotNull final Collection<Object> args) {
        super(logger, Level.INFO, marker, log, args);
    }

    public InfoLogEvent(@NotNull final Logger logger, @NotNull final Collection<Marker> marker, @NotNull final String log) {
        this(logger, marker, log, Collections.emptySet());
    }

    public InfoLogEvent(@NotNull final Logger logger, @NotNull final String log, @NotNull final Collection<Object> args) {
        this(logger, Collections.emptySet(), log, args);
    }

    public InfoLogEvent(@NotNull final Logger logger, @NotNull final String log) {
        this(logger, Collections.emptySet(), log, Collections.emptySet());
    }

    @Override
    public void fire() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEvent(() -> {
                listener.onInfoLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }

    @Override
    public void fireSynchronized() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventSync(() -> {
                listener.onInfoLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }

    @Override
    public void fireAsync() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventAsync(() -> {
                listener.onInfoLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }
}
