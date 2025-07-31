package dev.prodzeus.logger.event.events.log;

import dev.prodzeus.logger.Level;
import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.Marker;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public final class WarningLogEvent extends GenericLogEvent {

    public WarningLogEvent(@NotNull final Logger logger, @NotNull final Collection<Marker> marker, @NotNull final String log, @NotNull final Collection<Object> args) {
        super(logger, Level.WARNING, marker, log, args);
    }

    public WarningLogEvent(@NotNull final Logger logger, @NotNull final org.slf4j.Marker marker, @NotNull final String log, @NotNull final Object... args) {
        this(logger, Set.of(Marker.of(marker)), log, Set.of(args));
    }

    public WarningLogEvent(@NotNull final Logger logger, @NotNull final org.slf4j.Marker marker, @NotNull final String log, @NotNull final Object arg) {
        this(logger, Set.of(Marker.of(marker)), log, Set.of(arg));
    }

    public WarningLogEvent(@NotNull final Logger logger, @NotNull final String log, @NotNull final Object... arg) {
        this(logger, Collections.emptySet(), log, Set.of(arg));
    }

    public WarningLogEvent(@NotNull final Logger logger, @NotNull final String log, @NotNull final Object arg) {
        this(logger, Collections.emptySet(), log, Set.of(arg));
    }

    public WarningLogEvent(@NotNull final Logger logger, @NotNull final org.slf4j.Marker marker, @NotNull final String log) {
        this(logger, Set.of(Marker.of(marker)), log, Collections.emptySet());
    }

    public WarningLogEvent(@NotNull final Logger logger, @NotNull final String log) {
        this(logger, Collections.emptySet(), log, Collections.emptySet());
    }

    @Override
    public void fire() {
        fireEvent(this);
    }

    @Override
    public void fireSynchronized() {
        fireEventSync(this);
    }

    @Override
    public void fireAsync() {
        fireEventAsync(this);
    }
}
