package dev.prodzeus.logger.event.components;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.event.Event;
import dev.prodzeus.logger.event.events.exception.ExceptionEvent;
import dev.prodzeus.logger.event.events.log.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class EventListener {

    protected final Logger owner;
    public Logger getOwner() { return owner; }
    private final UUID uuid = UUID.randomUUID();

    protected EventListener(@NotNull final Logger logger) {
        this.owner = logger;
    }

    public void onGenericEvent(@NotNull final Event event) {}

    public void onExceptionEvent(@NotNull final ExceptionEvent event) {}

    // Logging Events
    public void onGenericLogEvent(@NotNull final GenericLogEvent event) {}

    public void onLogEvent(@NotNull final LogEvent event) {}

    public void onTraceLogEvent(@NotNull final TraceLogEvent event) {}

    public void onDebugLogEvent(@NotNull final DebugLogEvent event) {}

    public void onInfoLogEvent(@NotNull final InfoLogEvent event) {}

    public void onWarningLogEvent(@NotNull final WarningLogEvent event) {}

    public void onErrorLogEvent(@NotNull final ErrorLogEvent event) {}

    public void onExceptionLogEvent(@NotNull final ExceptionLogEvent event) {}

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof EventListener e && e.owner.equals(owner)&&e.uuid==uuid;
    }

    @Override
    public final int hashCode() {
        return owner.hashCode()*uuid.hashCode();
    }
}