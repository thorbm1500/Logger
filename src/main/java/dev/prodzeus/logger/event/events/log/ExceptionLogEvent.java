package dev.prodzeus.logger.event.events.log;

import dev.prodzeus.logger.Level;
import dev.prodzeus.logger.SLF4JProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ExceptionLogEvent extends GenericLogEvent {

    public ExceptionLogEvent(@NotNull final String log) {
        super(SLF4JProvider.getSystem(), Level.EXCEPTION, Collections.emptySet(), log, Collections.emptySet());
        fireSynchronized();
    }

    @Override
    protected void fire() {
        fireEvent(this);
    }

    @Override
    protected void fireSynchronized() {
        fireEventSync(this);
    }

    @Override
    protected void fireAsync() {
        fireEventAsync(this);
    }
}
