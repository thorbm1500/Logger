package dev.prodzeus.logger;

import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.events.exception.ExceptionEvent;
import dev.prodzeus.logger.event.events.log.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;

import java.util.*;
import java.util.regex.Matcher;

/**
 * A simple logger with SLF4J implementation and an Event Listening system inspired by Bukkit.
 *
 * @author prodzeus
 * @apiNote SLF4J Version: <b>2.0.12</b>
 */
public final class Logger implements org.slf4j.Logger {

    private final String name;
    private Level level = Level.INFO;
    private final Set<Marker> forcedMarkers = new HashSet<>();

    /**
     * Constructs a new Logger instance.
     *
     * @param name Name of the new Logger instance.
     * @apiNote <b>This should only be called through the LoggerFactory!</b>
     * @see LoggerFactory#getLogger(String)
     */
    public Logger(@NotNull final String name) {
        this.name = name;
        info("@greenNew Logger instance created.");
    }

    public Logger(@NotNull final Class<?> clazz) {
        this(clazz.getName());
    }

    /**
     * Register a forced {@link Marker}.
     *
     * @param marker Marker to register.
     * @return The Logger instance.
     * @apiNote A log with a forced marker will <b>always</b> be logged, regardless of the current {@link Level}.
     */
    public Logger registerForcedMarker(@NotNull final Marker marker) {
        this.forcedMarkers.add(marker);
        return this;
    }

    /**
     * Unregister a forced {@link Marker}.
     *
     * @param marker Marker to unregister.
     * @return The Logger instance.
     */
    public Logger unregisterForcedMarker(@NotNull final Marker marker) {
        this.forcedMarkers.remove(marker);
        return this;
    }

    /**
     * Clear all registered forced {@link Marker}s.
     *
     * @return The Logger instance.
     */
    public Logger clearForcedMarkers() {
        this.forcedMarkers.clear();
        return this;
    }

    /**
     * Set the current Log Level. Any log call below this level will be ignored.
     *
     * @param level New Log Level.
     * @return The Logger instance.
     */
    public Logger setLevel(@NotNull final Level level) {
        this.level = level;
        if (!this.equals(SLF4JProvider.getSystem())) SLF4JProvider.getSystem().setLevel(level);
        return this;
    }

    /**
     * Get the current log level set.
     * Any logs logged below this level will be ignored,
     * unless a forced marker has been registered.
     *
     * @return The current Level.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Contract(pure = true)
    public @NotNull Level getLevel() {
        return level;
    }

    public void suppressExceptions(final boolean suppress) {
        SLF4JProvider.get().suppressExceptions(suppress);
    }

    /**
     * Check if a level is loggable.
     *
     * @param level The Level to check.
     * @return True, if log calls at this level are logged to console, otherwise false.
     */
    @Contract(pure = true)
    public boolean isLoggable(@NotNull final Level level) {
        return isLoggable(level, null);
    }

    /**
     * Check if a level or marker is loggable.
     *
     * @param marker The marker.
     * @param level  The level.
     * @return True, if the level is equal to or higher than the current Log Level,
     * or if the marker is a registered forced marker, otherwise false.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Contract(pure = true)
    public boolean isLoggable(final Level level, @Nullable final Marker marker) {
        if (marker != null && forcedMarkers.contains(marker)) return true;
        return this.level.getWeight() <= level.getWeight();
    }

    /**
     * Get the name of the Logger instance.
     *
     * @return The name.
     */
    @Contract(pure = true)
    @Override
    public @NotNull String getName() {
        return name;
    }

    /**
     * Check if log calls to Log Level {@link Level#TRACE} will be logged or ignored.
     *
     * @return True | False
     */
    @Contract(pure = true)
    @Override
    public boolean isTraceEnabled() {
        return isLoggable(Level.TRACE);
    }

    @Override
    public void trace(@NotNull final String message) {
        new TraceLogEvent(this, message).fire();
    }

    public void traceSynchronized(@NotNull final String message) {
        new TraceLogEvent(this, message).fireSynchronized();
    }

    public void traceAsync(@NotNull final String message) {
        new TraceLogEvent(this, message).fireAsync();
    }

    @Override
    public void trace(@NotNull String message, @NotNull final Object arg) {
        new TraceLogEvent(this, format(message, arg), arg).fire();
    }

    public void traceSynchronized(@NotNull String message, @NotNull final Object arg) {
        new TraceLogEvent(this, format(message, arg), arg).fireSynchronized();
    }

    public void traceAsync(@NotNull String message, @NotNull final Object arg) {
        new TraceLogEvent(this, format(message, arg), arg).fireAsync();
    }

    @Override
    public void trace(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        new TraceLogEvent(this, format(message, arg1, arg2), arg1, arg2).fire();
    }

    public void traceSynchronized(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        new TraceLogEvent(this, format(message, arg1, arg2), arg1, arg2).fireSynchronized();
    }

    public void traceAsync(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        new TraceLogEvent(this, format(message, arg1, arg2), arg1, arg2).fireAsync();
    }

    @Override
    public void trace(@NotNull String message, @NotNull final Object... args) {
        new TraceLogEvent(this, format(message, args), args).fire();
    }

    public void traceSynchronized(@NotNull String message, @NotNull final Object... args) {
        new TraceLogEvent(this, format(message, args), args).fireSynchronized();
    }

    public void traceAsync(@NotNull String message, @NotNull final Object... args) {
        new TraceLogEvent(this, format(message, args), args).fireAsync();
    }

    @Override
    public void trace(@NotNull String message, @NotNull final Throwable t) {
        new TraceLogEvent(this, format(message, t), t).fire();
    }

    public void traceSynchronized(@NotNull String message, @NotNull final Throwable t) {
        new TraceLogEvent(this, format(message, t), t).fireSynchronized();
    }

    public void traceAsync(@NotNull String message, @NotNull final Throwable t) {
        new TraceLogEvent(this, format(message, t), t).fireAsync();
    }

    public void trace(@NotNull String message, @NotNull final EventException e) {
        new TraceLogEvent(this, format(message, e), e).fire();
    }

    public void traceSynchronized(@NotNull String message, @NotNull final EventException e) {
        new TraceLogEvent(this, format(message, e), e).fireSynchronized();
    }

    public void traceAsync(@NotNull String message, @NotNull final EventException e) {
        new TraceLogEvent(this, format(message, e), e).fireAsync();
    }

    /**
     * Check if log calls to Log Level {@link Level#TRACE} with the given {@link Marker} will be logged or ignored.
     *
     * @return True, if the current Log Level is of Level Trace, or if the Marker is a registered forced marker.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Contract(pure = true)
    @Override
    public boolean isTraceEnabled(@NotNull final Marker marker) {
        return isLoggable(Level.TRACE, marker);
    }

    @Override
    public void trace(@NotNull final Marker marker, @NotNull final String message) {
        new TraceLogEvent(this, marker, message).fire();
    }

    public void traceSynchronized(@NotNull final Marker marker, @NotNull final String message) {
        new TraceLogEvent(this, marker, message).fireSynchronized();
    }

    public void traceAsync(@NotNull final Marker marker, @NotNull final String message) {
        new TraceLogEvent(this, marker, message).fireAsync();
    }

    @Override
    public void trace(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new TraceLogEvent(this, marker, format(message, arg), arg).fire();
    }

    public void traceSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new TraceLogEvent(this, marker, format(message, arg), arg).fireSynchronized();
    }

    public void traceAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new TraceLogEvent(this, marker, format(message, arg), arg).fireAsync();
    }

    @Override
    public void trace(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new TraceLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fire();
    }

    public void traceSynchronized(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new TraceLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fireSynchronized();
    }

    public void traceAsync(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new TraceLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fireAsync();
    }

    @Override
    public void trace(@NotNull final Marker marker, String message, Object... args) {
        new TraceLogEvent(this, marker, format(message, args), args).fire();
    }

    public void traceSynchronized(@NotNull final Marker marker, String message, Object... args) {
        new TraceLogEvent(this, marker, format(message, args), args).fireSynchronized();
    }

    public void traceAsync(@NotNull final Marker marker, String message, Object... args) {
        new TraceLogEvent(this, marker, format(message, args), args).fireAsync();
    }

    @Override
    public void trace(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        new TraceLogEvent(this, marker, format(message, t), t).fire();
    }

    public void traceSynchronized(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        new TraceLogEvent(this, marker, format(message, t), t).fireSynchronized();
    }

    public void traceAsync(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        new TraceLogEvent(this, marker, format(message, t), t).fireAsync();
    }

    public void trace(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        new TraceLogEvent(this, marker, format(message, e), e).fire();
    }

    public void traceSynchronized(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        new TraceLogEvent(this, marker, format(message, e), e).fireSynchronized();
    }

    public void traceAsync(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        new TraceLogEvent(this, marker, format(message, e), e).fireAsync();
    }

    /**
     * Check if log calls to Log Level {@link Level#DEBUG} will be logged or ignored.
     *
     * @return True | False
     */
    @Contract(pure = true)
    @Override
    public boolean isDebugEnabled() {
        return isLoggable(Level.DEBUG);
    }

    @Override
    public void debug(@NotNull final String message) {
        new DebugLogEvent(this, message).fire();
    }

    public void debugSynchronized(@NotNull final String message) {
        new DebugLogEvent(this, message).fireSynchronized();
    }

    public void debugAsync(@NotNull final String message) {
        new DebugLogEvent(this, message).fireAsync();
    }

    @Override
    public void debug(@NotNull final String message, @NotNull final Object arg) {
        new DebugLogEvent(this, format(message, arg), arg).fire();
    }

    public void debugSynchronized(@NotNull final String message, @NotNull final Object arg) {
        new DebugLogEvent(this, format(message, arg), arg).fireSynchronized();
    }

    public void debugAsync(@NotNull final String message, @NotNull final Object arg) {
        new DebugLogEvent(this, format(message, arg), arg).fireAsync();
    }

    @Override
    public void debug(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new DebugLogEvent(this, format(message, arg1, arg2), arg1, arg2).fire();
    }

    public void debugSynchronized(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new DebugLogEvent(this, format(message, arg1, arg2), arg1, arg2).fireSynchronized();
    }

    public void debugAsync(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new DebugLogEvent(this, format(message, arg1, arg2), arg1, arg2).fireAsync();
    }

    @Override
    public void debug(@NotNull final String message, @NotNull final Object... args) {
        new DebugLogEvent(this, format(message, args), args).fire();
    }

    public void debugSynchronized(@NotNull final String message, @NotNull final Object... args) {
        new DebugLogEvent(this, format(message, args), args).fireSynchronized();
    }

    public void debugAsync(@NotNull final String message, @NotNull final Object... args) {
        new DebugLogEvent(this, format(message, args), args).fireAsync();
    }

    @Override
    public void debug(@NotNull final String message, @NotNull final Throwable t) {
        new DebugLogEvent(this, format(message, t), t).fire();
    }

    public void debugSynchronized(@NotNull final String message, @NotNull final Throwable t) {
        new DebugLogEvent(this, format(message, t), t).fireSynchronized();
    }

    public void debugAsync(@NotNull final String message, @NotNull final Throwable t) {
        new DebugLogEvent(this, format(message, t), t).fireAsync();
    }

    public void debug(@NotNull final String message, @NotNull final EventException e) {
        new DebugLogEvent(this, format(message, e), e).fire();
    }

    public void debugSynchronized(@NotNull final String message, @NotNull final EventException e) {
        new DebugLogEvent(this, format(message, e), e).fireSynchronized();
    }

    public void debugAsync(@NotNull final String message, @NotNull final EventException e) {
        new DebugLogEvent(this, format(message, e), e).fireAsync();
    }

    /**
     * Check if log calls to Log Level {@link Level#DEBUG} with the given {@link Marker} will be logged or ignored.
     *
     * @return True, if the current Log Level is of Level Debug, or if the Marker is a registered forced marker.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Contract(pure = true)
    @Override
    public boolean isDebugEnabled(@NotNull final Marker marker) {
        return isLoggable(Level.DEBUG, marker);
    }

    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message) {
        new DebugLogEvent(this, marker, message).fire();
    }

    public void debugSynchronized(@NotNull final Marker marker, @NotNull final String message) {
        new DebugLogEvent(this, marker, message).fireSynchronized();
    }

    public void debugAsync(@NotNull final Marker marker, @NotNull final String message) {
        new DebugLogEvent(this, marker, message).fireAsync();
    }

    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new DebugLogEvent(this, marker, format(message, arg), arg).fire();
    }

    public void debugSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new DebugLogEvent(this, marker, format(message, arg), arg).fireSynchronized();
    }

    public void debugAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new DebugLogEvent(this, marker, format(message, arg), arg).fireAsync();
    }

    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new DebugLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fire();
    }

    public void debugSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new DebugLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fireSynchronized();
    }


    public void debugAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new DebugLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fireAsync();
    }


    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        new DebugLogEvent(this, marker, format(message, args), args).fire();
    }

    public void debugSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        new DebugLogEvent(this, marker, format(message, args), args).fireSynchronized();
    }

    public void debugAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        new DebugLogEvent(this, marker, format(message, args), args).fireAsync();
    }

    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        new DebugLogEvent(this, marker, format(message, t), t).fire();
    }

    public void debugSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        new DebugLogEvent(this, marker, format(message, t), t).fireSynchronized();
    }

    public void debugAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        new DebugLogEvent(this, marker, format(message, t), t).fireAsync();
    }

    public void debug(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        new DebugLogEvent(this, marker, format(message, e), e).fire();
    }

    public void debugSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        new DebugLogEvent(this, marker, format(message, e), e).fireSynchronized();
    }

    public void debugAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        new DebugLogEvent(this, marker, format(message, e), e).fireAsync();
    }

    /**
     * Check if log calls to Log Level {@link Level#INFO} will be logged or ignored.
     *
     * @return True | False
     */
    @Contract(pure = true)
    @Override
    public boolean isInfoEnabled() {
        return isLoggable(Level.INFO);
    }

    @Override
    public void info(@NotNull final String message) {
        new InfoLogEvent(this, message).fire();
    }

    public void infoSynchronized(@NotNull final String message) {
        new InfoLogEvent(this, message).fireSynchronized();
    }

    public void infoAsync(@NotNull final String message) {
        new InfoLogEvent(this, message).fireAsync();
    }


    @Override
    public void info(@NotNull final String message, @NotNull final Object arg) {
        new InfoLogEvent(this, format(message, arg), arg).fire();
    }

    public void infoSynchronized(@NotNull final String message, @NotNull final Object arg) {
        new InfoLogEvent(this, format(message, arg), arg).fireSynchronized();
    }

    public void infoAsync(@NotNull final String message, @NotNull final Object arg) {
        new InfoLogEvent(this, format(message, arg), arg).fireAsync();
    }

    @Override
    public void info(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new InfoLogEvent(this, format(message, arg1, arg2), arg1, arg2).fire();
    }

    public void infoSynchronized(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new InfoLogEvent(this, format(message, arg1, arg2), arg1, arg2).fireSynchronized();
    }

    public void infoAsync(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new InfoLogEvent(this, format(message, arg1, arg2), arg1, arg2).fireAsync();
    }

    @Override
    public void info(@NotNull final String message, @NotNull final Object... args) {
        new InfoLogEvent(this, format(message, args), args).fire();
    }

    public void infoSynchronized(@NotNull final String message, @NotNull final Object... args) {
        new InfoLogEvent(this, format(message, args), args).fireSynchronized();
    }

    public void infoAsync(@NotNull final String message, @NotNull final Object... args) {
        new InfoLogEvent(this, format(message, args), args).fireAsync();
    }

    @Override
    public void info(@NotNull final String message, @NotNull final Throwable t) {
        new InfoLogEvent(this, format(message, t), t).fire();
    }

    public void infoSynchronized(@NotNull final String message, @NotNull final Throwable t) {
        new InfoLogEvent(this, format(message, t), t).fireSynchronized();
    }

    public void infoAsync(@NotNull final String message, @NotNull final Throwable t) {
        new InfoLogEvent(this, format(message, t), t).fireAsync();
    }

    public void info(@NotNull final String message, @NotNull final EventException e) {
        new InfoLogEvent(this, format(message, e), e).fire();
    }

    public void infoSynchronized(@NotNull final String message, @NotNull final EventException e) {
        new InfoLogEvent(this, format(message, e), e).fireSynchronized();
    }

    public void infoAsync(@NotNull final String message, @NotNull final EventException e) {
        new InfoLogEvent(this, format(message, e), e).fireAsync();
    }

    /**
     * Check if log calls to Log Level {@link Level#INFO} with the given {@link Marker} will be logged or ignored.
     *
     * @return True, if the current Log Level is of Level Info, or if the Marker is a registered forced marker.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Contract(pure = true)
    @Override
    public boolean isInfoEnabled(@NotNull final Marker marker) {
        return isLoggable(Level.INFO, marker);
    }

    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message) {
        new InfoLogEvent(this, marker, message).fire();
    }

    public void infoSynchronized(@NotNull final Marker marker, @NotNull final String message) {
        new InfoLogEvent(this, marker, message).fireSynchronized();
    }

    public void infoAsync(@NotNull final Marker marker, @NotNull final String message) {
        new InfoLogEvent(this, marker, message).fireAsync();
    }

    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new InfoLogEvent(this, marker, format(message, arg), arg).fire();
    }

    public void infoSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new InfoLogEvent(this, marker, format(message, arg), arg).fireSynchronized();
    }

    public void infoAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new InfoLogEvent(this, marker, format(message, arg), arg).fireAsync();
    }

    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new InfoLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fire();
    }

    public void infoSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new InfoLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fireSynchronized();
    }

    public void infoAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new InfoLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fireAsync();
    }

    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        new InfoLogEvent(this, marker, format(message, args), args).fire();
    }

    public void infoSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        new InfoLogEvent(this, marker, format(message, args), args).fireSynchronized();
    }

    public void infoAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        new InfoLogEvent(this, marker, format(message, args), args).fireAsync();
    }

    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        new InfoLogEvent(this, marker, format(message, t), t).fire();
    }

    public void infoSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        new InfoLogEvent(this, marker, format(message, t), t).fireSynchronized();
    }

    public void infoAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        new InfoLogEvent(this, marker, format(message, t), t).fireAsync();
    }

    public void info(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        new InfoLogEvent(this, marker, format(message, e), e).fire();
    }

    public void infoSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        new InfoLogEvent(this, marker, format(message, e), e).fireSynchronized();
    }

    public void infoAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        new InfoLogEvent(this, marker, format(message, e), e).fireAsync();
    }

    /**
     * Check if log calls to Log Level {@link Level#WARNING} will be logged or ignored.
     *
     * @return True | False
     */
    @Contract(pure = true)
    @Override
    public boolean isWarnEnabled() {
        return isLoggable(Level.WARNING);
    }

    @Contract(pure = true)
    public boolean isWarningEnabled() {
        return isWarnEnabled();
    }

    @Override
    public void warn(@NotNull final String message) {
        new WarningLogEvent(this, message).fire();
    }

    public void warnSynchronized(@NotNull final String message) {
        new WarningLogEvent(this, message).fireSynchronized();
    }

    public void warnAsync(@NotNull final String message) {
        new WarningLogEvent(this, message).fireAsync();
    }

    public void warning(@NotNull final String message) {
        warn(message);
    }

    public void warningSynchronized(@NotNull final String message) {
        warnSynchronized(message);
    }

    public void warningAsync(@NotNull final String message) {
        warnAsync(message);
    }

    @Override
    public void warn(@NotNull final String message, @NotNull final Object arg) {
        new WarningLogEvent(this, format(message, arg), arg).fire();
    }

    public void warnSynchronized(@NotNull final String message, @NotNull final Object arg) {
        new WarningLogEvent(this, format(message, arg), arg).fireSynchronized();
    }

    public void warnAsync(@NotNull final String message, @NotNull final Object arg) {
        new WarningLogEvent(this, format(message, arg), arg).fireAsync();
    }

    public void warning(@NotNull final String message, @NotNull final Object arg) {
        warn(message, arg);
    }

    public void warningSynchronized(@NotNull final String message, @NotNull final Object arg) {
        warnSynchronized(message, arg);
    }

    public void warningAsync(@NotNull final String message, @NotNull final Object arg) {
        warnAsync(message, arg);
    }

    @Override
    public void warn(@NotNull final String message, @NotNull final Object... args) {
        new WarningLogEvent(this, format(message, args), args).fire();
    }

    public void warnSynchronized(@NotNull final String message, @NotNull final Object... args) {
        new WarningLogEvent(this, format(message, args), args).fireSynchronized();
    }

    public void warnAsync(@NotNull final String message, @NotNull final Object... args) {
        new WarningLogEvent(this, format(message, args), args).fireAsync();
    }

    public void warning(@NotNull final String message, @NotNull final Object... arguments) {
        warn(message, arguments);
    }

    public void warningSynchronized(@NotNull final String message, @NotNull final Object... arguments) {
        warnSynchronized(message, arguments);
    }

    public void warningAsync(@NotNull final String message, @NotNull final Object... arguments) {
        warnAsync(message, arguments);
    }

    @Override
    public void warn(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new WarningLogEvent(this, format(message, arg1, arg2), arg1, arg2).fire();
    }

    public void warnSynchronized(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new WarningLogEvent(this, format(message, arg1, arg2), arg1, arg2).fireSynchronized();
    }

    public void warnAsync(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new WarningLogEvent(this, format(message, arg1, arg2), arg1, arg2).fireAsync();
    }

    public void warning(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        warn(message, arg1, arg2);
    }

    public void warningSynchronized(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        warnSynchronized(message, arg1, arg2);
    }

    public void warningAsync(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        warnAsync(message, arg1, arg2);
    }

    @Override
    public void warn(@NotNull final String message, @NotNull final Throwable t) {
        new WarningLogEvent(this, format(message, t), t).fire();
    }

    public void warnSynchronized(@NotNull final String message, @NotNull final Throwable t) {
        new WarningLogEvent(this, format(message, t), t).fireSynchronized();
    }

    public void warnAsync(@NotNull final String message, @NotNull final Throwable t) {
        new WarningLogEvent(this, format(message, t), t).fireAsync();
    }

    public void warning(@NotNull final String message, @NotNull final Throwable t) {
        warn(message, t);
    }

    public void warningSynchronized(@NotNull final String message, @NotNull final Throwable t) {
        warnSynchronized(message, t);
    }

    public void warningAsync(@NotNull final String message, @NotNull final Throwable t) {
        warnAsync(message, t);
    }

    public void warn(@NotNull final String message, @NotNull final EventException e) {
        new WarningLogEvent(this, format(message, e), e).fire();
    }

    public void warnSynchronized(@NotNull final String message, @NotNull final EventException e) {
        new WarningLogEvent(this, format(message, e), e).fireSynchronized();
    }

    public void warnAsync(@NotNull final String message, @NotNull final EventException e) {
        new WarningLogEvent(this, format(message, e), e).fireAsync();
    }

    public void warning(@NotNull final String message, @NotNull final EventException e) {
        warn(message, e);
    }

    public void warningSynchronized(@NotNull final String message, @NotNull final EventException e) {
        warnSynchronized(message, e);
    }

    public void warningAsync(@NotNull final String message, @NotNull final EventException e) {
        warnAsync(message, e);
    }

    /**
     * Check if log calls to Log Level {@link Level#WARNING} with the given {@link Marker} will be logged or ignored.
     *
     * @return True, if the current Log Level is of Level Warning, or if the Marker is a registered forced marker.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Contract(pure = true)
    @Override
    public boolean isWarnEnabled(@NotNull final Marker marker) {
        return isLoggable(Level.WARNING, marker);
    }

    @Contract(pure = true)
    public boolean isWarningEnabled(@NotNull final Marker marker) {
        return isWarnEnabled(marker);
    }

    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message) {
        new WarningLogEvent(this, marker, message).fire();
    }

    public void warnSynchronized(@NotNull final Marker marker, @NotNull final String message) {
        new WarningLogEvent(this, marker, message).fireSynchronized();
    }

    public void warnAsync(@NotNull final Marker marker, @NotNull final String message) {
        new WarningLogEvent(this, marker, message).fireAsync();
    }

    public void warning(@NotNull final Marker marker, @NotNull final String message) {
        warn(marker, message);
    }

    public void warningSynchronized(@NotNull final Marker marker, @NotNull final String message) {
        warnSynchronized(marker, message);
    }

    public void warningAsync(@NotNull final Marker marker, @NotNull final String message) {
        warnAsync(marker, message);
    }

    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new WarningLogEvent(this, marker, format(message, arg), arg).fire();
    }

    public void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new WarningLogEvent(this, marker, format(message, arg), arg).fireSynchronized();
    }

    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new WarningLogEvent(this, marker, format(message, arg), arg).fireAsync();
    }

    public void warning(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        warn(marker, message, arg);
    }

    public void warningSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        warnSynchronized(marker, message, arg);
    }

    public void warningAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        warnAsync(marker, message, arg);
    }

    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new WarningLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fire();
    }

    public void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new WarningLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fireSynchronized();
    }

    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new WarningLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fireAsync();
    }

    public void warning(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        warn(marker, message, arg1, arg2);
    }

    public void warningSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        warnSynchronized(marker, message, arg1, arg2);
    }

    public void warningAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        warnAsync(marker, message, arg1, arg2);
    }

    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        new WarningLogEvent(this, marker, format(message, args), args).fire();
    }

    public void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        new WarningLogEvent(this, marker, format(message, args), args).fireSynchronized();
    }

    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        new WarningLogEvent(this, marker, format(message, args), args).fireAsync();
    }

    public void warning(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... arguments) {
        warn(marker, message, arguments);
    }

    public void warningSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... arguments) {
        warnSynchronized(marker, message, arguments);
    }

    public void warningAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... arguments) {
        warnAsync(marker, message, arguments);
    }

    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        new WarningLogEvent(this, marker, format(message, t), t).fire();
    }

    public void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        new WarningLogEvent(this, marker, format(message, t), t).fireSynchronized();
    }

    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        new WarningLogEvent(this, marker, format(message, t), t).fireAsync();
    }

    public void warning(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        warn(marker, message, t);
    }

    public void warningSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        warnSynchronized(marker, message, t);
    }

    public void warningAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        warnAsync(marker, message, t);
    }

    public void warn(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        new WarningLogEvent(this, marker, format(message, e), e).fire();
    }

    public void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        new WarningLogEvent(this, marker, format(message, e), e).fireSynchronized();
    }

    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        new WarningLogEvent(this, marker, format(message, e), e).fireAsync();
    }

    public void warning(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        warn(marker, message, e);
    }

    public void warningSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        warnSynchronized(marker, message, e);
    }

    public void warningAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        warnAsync(marker, message, e);
    }

    /**
     * Check if log calls to Log Level {@link Level#ERROR} will be logged or ignored.
     *
     * @return True | False
     */
    @Contract(pure = true)
    @Override
    public boolean isErrorEnabled() {
        return isLoggable(Level.ERROR);
    }

    @Override
    public void error(@NotNull final String message) {
        new ErrorLogEvent(this, message).fire();
    }

    public void errorSynchronized(@NotNull final String message) {
        new ErrorLogEvent(this, message).fireSynchronized();
    }

    public void errorAsync(@NotNull final String message) {
        new ErrorLogEvent(this, message).fireAsync();
    }

    @Override
    public void error(@NotNull final String message, @NotNull final Object arg) {
        new ErrorLogEvent(this, format(message, arg), arg).fire();
    }

    public void errorSynchronized(@NotNull final String message, @NotNull final Object arg) {
        new ErrorLogEvent(this, format(message, arg), arg).fireSynchronized();
    }

    public void errorAsync(@NotNull final String message, @NotNull final Object arg) {
        new ErrorLogEvent(this, format(message, arg), arg).fireAsync();
    }

    @Override
    public void error(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new ErrorLogEvent(this, format(message, arg1, arg2), arg1, arg2).fire();
    }

    public void errorSynchronized(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new ErrorLogEvent(this, format(message, arg1, arg2), arg1, arg2).fireSynchronized();
    }

    public void errorAsync(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new ErrorLogEvent(this, format(message, arg1, arg2), arg1, arg2).fireAsync();
    }


    @Override
    public void error(@NotNull final String message, @NotNull final Object... args) {
        new ErrorLogEvent(this, format(message, args), args).fire();
    }

    public void errorSynchronized(@NotNull final String message, @NotNull final Object... args) {
        new ErrorLogEvent(this, format(message, args), args).fireSynchronized();
    }

    public void errorAsync(@NotNull final String message, @NotNull final Object... args) {
        new ErrorLogEvent(this, format(message, args), args).fireAsync();
    }

    @Override
    public void error(@NotNull final String message, @NotNull final Throwable t) {
        new ErrorLogEvent(this, format(message, t), t).fire();
    }

    public void errorSynchronized(@NotNull final String message, @NotNull final Throwable t) {
        new ErrorLogEvent(this, format(message, t), t).fireSynchronized();
    }

    public void errorAsync(@NotNull final String message, @NotNull final Throwable t) {
        new ErrorLogEvent(this, format(message, t), t).fireAsync();
    }

    public void error(@NotNull final String message, @NotNull final EventException e) {
        new ErrorLogEvent(this, format(message, e), e).fire();
    }

    public void errorSynchronized(@NotNull final String message, @NotNull final EventException e) {
        new ErrorLogEvent(this, format(message, e), e).fireSynchronized();
    }

    public void errorAsync(@NotNull final String message, @NotNull final EventException e) {
        new ErrorLogEvent(this, format(message, e), e).fireAsync();
    }

    /**
     * Check if log calls to Log Level {@link Level#ERROR} with the given {@link Marker} will be logged or ignored.
     *
     * @return True, if the current Log Level is of Level Error, or if the Marker is a registered forced marker.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Contract(pure = true)
    @Override
    public boolean isErrorEnabled(@NotNull final Marker marker) {
        return isLoggable(Level.ERROR, marker);
    }

    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message) {
        new ErrorLogEvent(this, marker, message).fire();
    }

    public void errorSynchronized(@NotNull final Marker marker, @NotNull final String message) {
        new ErrorLogEvent(this, marker, message).fireSynchronized();
    }

    public void errorAsync(@NotNull final Marker marker, @NotNull final String message) {
        new ErrorLogEvent(this, marker, message).fireAsync();
    }

    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new ErrorLogEvent(this, marker, format(message, arg), arg).fire();
    }

    public void errorSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new ErrorLogEvent(this, marker, format(message, arg), arg).fireSynchronized();
    }

    public void errorAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        new ErrorLogEvent(this, marker, format(message, arg), arg).fireAsync();
    }

    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new ErrorLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fire();
    }

    public void errorSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new ErrorLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fireSynchronized();
    }

    public void errorAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        new ErrorLogEvent(this, marker, format(message, arg1, arg2), arg1, arg2).fireAsync();
    }

    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        new ErrorLogEvent(this, marker, format(message, args), args).fire();
    }

    public void errorSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        new ErrorLogEvent(this, marker, format(message, args), args).fireSynchronized();
    }

    public void errorAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        new ErrorLogEvent(this, marker, format(message, args), args).fireAsync();
    }

    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        new ErrorLogEvent(this, marker, format(message, t), t).fire();
    }

    public void errorSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        new ErrorLogEvent(this, marker, format(message, t), t).fireSynchronized();
    }

    public void errorAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        new ErrorLogEvent(this, marker, format(message, t), t).fireAsync();
    }

    public void error(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        new ErrorLogEvent(this, marker, format(message, e), e).fire();
    }

    public void errorSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        new ErrorLogEvent(this, marker, format(message, e), e).fireSynchronized();
    }

    public void errorAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        new ErrorLogEvent(this, marker, format(message, e), e).fireAsync();
    }

    @Contract(pure = true)
    private static @NotNull String format(@NotNull final String log, @NotNull final Object... args) {
        String message = log.replaceAll("%[s|d]", Matcher.quoteReplacement("{}"))
                .replace("%d", Matcher.quoteReplacement("{}"))
                .replaceAll("(%(\\.\\d)?f)", Matcher.quoteReplacement("{}"));
        for (Object arg : args) {
            if (arg instanceof EventException e) arg = e.getCause();
            switch (arg) {
                case null -> {
                    message = message.replaceFirst("\\{}", "null");
                }
                case Throwable t -> {
                    final StringBuilder builder = new StringBuilder();
                    builder.append(Level.ERROR.getColor()).append(t.getMessage()).append("\n");
                    for (final StackTraceElement st : t.getStackTrace()) {
                        builder.append(Level.ERROR.getColor()).append(st.toString()).append("\n");
                    }
                    if (!message.contains("{}")) {
                        new ExceptionLogEvent(builder.toString());
                    } else message = message.replaceFirst("\\{}", Matcher.quoteReplacement(builder.toString()));
                }
                case Collection<?> c -> {
                    final StringBuilder builder = new StringBuilder();
                    builder.append("[ ");
                    final Iterator<?> iterator = c.iterator();
                    while (iterator.hasNext()) {
                        builder.append(iterator.next());
                        if (iterator.hasNext()) builder.append(", ");
                    }
                    builder.append("]");
                    message = message.replaceFirst("\\{}", Matcher.quoteReplacement(builder.toString()));
                }
                case Map<?, ?> m -> {
                    final StringBuilder builder = new StringBuilder();
                    builder.append("{");
                    for (final var index : m.entrySet()) {
                        builder.append(" [ %s , %s ] ".formatted(String.valueOf(index.getKey()), String.valueOf(index.getValue())));
                    }
                    builder.append("}");
                    message = message.replaceFirst("\\{}", Matcher.quoteReplacement(builder.toString()));
                }
                /*case Number n -> {
                    if (!message.contains("\\{}"))
                    if (Pattern.matches("(\\{-([a-zA-Z]){1,3}})",message)) {
                        String replacement = Pattern.compile("(\\{-([a-zA-Z]){1,3}})").matcher(message).toMatchResult().toString();
                        message = message.replaceFirst("(\\{-([a-zA-Z]){1,3}})", Matcher.quoteReplacement(replacement));
                    }
                }*/
                case String s -> message = message.replaceFirst("\\{}", Matcher.quoteReplacement(s));
                default -> message = message.replaceFirst("\\{}", Matcher.quoteReplacement(arg.toString()));
            }
        }
        return message;
    }
}