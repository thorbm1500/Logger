package dev.prodzeus.logger.event.events.log;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.components.Level;
import dev.prodzeus.logger.event.components.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;

import java.util.Collection;
import java.util.Collections;

public final class WarningLogEvent extends GenericLogEvent {

    public WarningLogEvent(@NotNull final Logger logger, @NotNull final Collection<Marker> marker, @NotNull final String log, @NotNull final Collection<Object> args) {
        super(logger, Level.WARNING, marker, log, args);
    }

    public WarningLogEvent(@NotNull final Logger logger, @NotNull final Collection<Marker> marker, @NotNull final String log) {
        this(logger, marker, log, Collections.emptySet());
    }

    public WarningLogEvent(@NotNull final Logger logger, @NotNull final String log, @NotNull final Collection<Object> args) {
        this(logger, Collections.emptySet(), log, args);
    }

    public WarningLogEvent(@NotNull final Logger logger, @NotNull final String log) {
        this(logger, Collections.emptySet(), log, Collections.emptySet());
    }

    @Override
    public void fire() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEvent(() -> {
                listener.onWarningLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }

    @Override
    public void fireSynchronized() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventSync(() -> {
                listener.onWarningLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }

    @Override
    public void fireAsync() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventAsync(() -> {
                listener.onWarningLogEvent(this);
                listener.onGenericEvent(this);
            });
        }
    }
}
