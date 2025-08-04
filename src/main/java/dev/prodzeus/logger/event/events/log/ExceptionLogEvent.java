package dev.prodzeus.logger.event.events.log;

import dev.prodzeus.logger.Level;
import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.event.components.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public final class ExceptionLogEvent extends GenericLogEvent {

    public ExceptionLogEvent(@NotNull final String log) {
        super(SLF4JProvider.getSystem(), Level.EXCEPTION, Collections.emptySet(), log, Collections.emptySet());
    }

    public ExceptionLogEvent(@NotNull final Throwable cause) {
        super(cause);
    }

    @Override
    public void fire() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEvent(() -> listener.onExceptionLogEvent(this));
        }
    }

    @Override
    public void fireSynchronized() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventSync(() -> listener.onExceptionLogEvent(this));
        }
    }

    @Override
    public void fireAsync() {
        for (@NotNull final EventListener listener : getListeners()) {
            fireEventAsync(() -> listener.onExceptionLogEvent(this));
        }
    }
}
