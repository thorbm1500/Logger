package dev.prodzeus.logger;

import dev.prodzeus.logger.components.Level;
import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.events.log.*;
import dev.prodzeus.logger.slf4j.SLF4JProvider;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;

import java.util.*;
import java.util.regex.Matcher;

import static dev.prodzeus.logger.components.Level.*;

/**
 * A simple logger with SLF4J implementation and an Event Listening system inspired by Bukkit.
 *
 * @author prodzeus
 * @apiNote SLF4J Version: <b>2.0.12</b>
 */
public class Logger implements org.slf4j.Logger {

    protected final String name;
    protected Level level = INFO;
    private final Set<Marker> forcedMarkers = new HashSet<>();

    /**
     * Constructs a new Logger instance.
     *
     * @param name Name of the new Logger instance.
     * @apiNote <b>This should only be called through the LoggerFactory!</b>
     * @see dev.prodzeus.logger.slf4j.LoggerFactory#getLogger(String) 
     */
    public Logger(@NotNull final String name) {
        this.name = name;
        SLF4JProvider.get().getLoggerFactory().validate(this);
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
        SLF4JProvider.getSystem().setLevel(level);
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

    private void validateAndFire(@NotNull Level level, @NotNull final String message) {
        validateAndFire(level, message, Collections.emptySet());
    }

    private void validateAndFire(@NotNull Level level, @NotNull final Marker marker, @NotNull final String message) {
        validateAndFire(level, Set.of(marker), message, Collections.emptySet());
    }

    private void validateAndFire(@NotNull Level level, @NotNull final String message, @NotNull Collection<Object> args) {
        validateAndFire(level, Collections.emptySet(), message, args);
    }

    private void validateAndFire(@NotNull Level level, @NotNull final Marker marker, @NotNull final String message, @NotNull Collection<Object> args) {
        validateAndFire(level, Set.of(marker), message, args);
    }

    private void validateAndFire(@NotNull Level level, @NotNull Collection<Marker> markers, @NotNull final String message, @NotNull Collection<Object> args) {
        if (args.stream().anyMatch(Throwable.class::isInstance)) {
            new ExceptionLogEvent(this, markers, message, args).fire();
        } else switch (level) {
            case OFF -> {/* Ignore. */}
            case TRACE -> new TraceLogEvent(this, markers, message, args).fire();
            case DEBUG -> new DebugLogEvent(this, markers, message, args).fire();
            case INFO -> new InfoLogEvent(this, markers, message, args).fire();
            case WARNING ->  new WarningLogEvent(this, markers, message, args).fire();
            case ERROR -> new ErrorLogEvent(this, markers, message, args).fire();
            case ALL -> new LogEvent(this, markers, message, args);
        }
    }

    private void validateAndFireSynchronized(@NotNull Level level, @NotNull final String message) {
        validateAndFireSynchronized(level, message, Collections.emptySet());
    }

    private void validateAndFireSynchronized(@NotNull Level level, @NotNull final Marker marker, @NotNull final String message) {
        validateAndFireSynchronized(level, Set.of(marker), message, Collections.emptySet());
    }

    private void validateAndFireSynchronized(@NotNull Level level, @NotNull final String message, @NotNull Collection<Object> args) {
        validateAndFireSynchronized(level, Collections.emptySet(), message, args);
    }

    private void validateAndFireSynchronized(@NotNull Level level, @NotNull final Marker marker, @NotNull final String message, @NotNull Collection<Object> args) {
        validateAndFireSynchronized(level, Set.of(marker), message, args);
    }

    private void validateAndFireSynchronized(@NotNull Level level, @NotNull Collection<Marker> markers, @NotNull final String message, @NotNull Collection<Object> args) {
        if (args.stream().anyMatch(Throwable.class::isInstance)) {
            new ExceptionLogEvent(this, markers, message, args).fireSynchronized();
        } else switch (level) {
            case OFF -> {/* Ignore. */}
            case TRACE -> new TraceLogEvent(this, markers, message, args).fireSynchronized();
            case DEBUG -> new DebugLogEvent(this, markers, message, args).fireSynchronized();
            case INFO -> new InfoLogEvent(this, markers, message, args).fireSynchronized();
            case WARNING ->  new WarningLogEvent(this, markers, message, args).fireSynchronized();
            case ERROR -> new ErrorLogEvent(this, markers, message, args).fireSynchronized();
            case ALL -> new LogEvent(this, markers, message, args);
        }
    }

    private void validateAndFireAsync(@NotNull Level level, @NotNull final String message) {
        validateAndFireAsync(level, message, Collections.emptySet());
    }

    private void validateAndFireAsync(@NotNull Level level, @NotNull final Marker marker, @NotNull final String message) {
        validateAndFireAsync(level, Set.of(marker), message, Collections.emptySet());
    }

    private void validateAndFireAsync(@NotNull Level level, @NotNull final String message, @NotNull Collection<Object> args) {
        validateAndFireAsync(level, Collections.emptySet(), message, args);
    }

    private void validateAndFireAsync(@NotNull Level level, @NotNull final Marker marker, @NotNull final String message, @NotNull Collection<Object> args) {
        validateAndFireAsync(level, Set.of(marker), message, args);
    }

    private void validateAndFireAsync(@NotNull Level level, @NotNull Collection<Marker> markers, @NotNull final String message, @NotNull Collection<Object> args) {
        if (args.stream().anyMatch(Throwable.class::isInstance)) {
            new ExceptionLogEvent(this, markers, message, args).fireAsync();
        } else switch (level) {
            case OFF -> {/* Ignore. */}
            case TRACE -> new TraceLogEvent(this, markers, message, args).fireAsync();
            case DEBUG -> new DebugLogEvent(this, markers, message, args).fireAsync();
            case INFO -> new InfoLogEvent(this, markers, message, args).fireAsync();
            case WARNING ->  new WarningLogEvent(this, markers, message, args).fireAsync();
            case ERROR -> new ErrorLogEvent(this, markers, message, args).fireAsync();
            case ALL -> new LogEvent(this, markers, message, args);
        }
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
        validateAndFire(TRACE, message);
    }

    public void traceSynchronized(@NotNull final String message) {
        validateAndFireSynchronized(TRACE, message);
    }

    public void traceAsync(@NotNull final String message) {
        validateAndFireAsync(TRACE, message);
    }

    @Override
    public void trace(@NotNull String message, @NotNull final Object arg) {
        validateAndFire(TRACE, format(message, arg), Set.of(arg));
    }

    public void traceSynchronized(@NotNull String message, @NotNull final Object arg) {
        validateAndFireSynchronized(TRACE, format(message, arg), Set.of(arg));
    }

    public void traceAsync(@NotNull String message, @NotNull final Object arg) {
        validateAndFireAsync(TRACE, format(message, arg), Set.of(arg));
    }

    @Override
    public void trace(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        validateAndFire(TRACE, format(message, arg1, arg2), Set.of(arg1, arg2));
    }

    public void traceSynchronized(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        validateAndFireSynchronized(TRACE, format(message, arg1, arg2), Set.of(arg1, arg2));
    }

    public void traceAsync(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        validateAndFireAsync(TRACE, format(message, arg1, arg2), Set.of(arg1, arg2));
    }

    @Override
    public void trace(@NotNull String message, @NotNull final Object... args) {
        validateAndFire(TRACE, format(message, args), Set.of(args));
    }

    public void traceSynchronized(@NotNull String message, @NotNull final Object... args) {
        validateAndFireSynchronized(TRACE, format(message, args), Set.of(args));
    }

    public void traceAsync(@NotNull String message, @NotNull final Object... args) {
        validateAndFireAsync(TRACE, format(message, args), Set.of(args));
    }

    @Override
    public void trace(@NotNull String message, @NotNull final Throwable t) {
        validateAndFire(TRACE, format(message, t), Set.of(t));
    }

    public void traceSynchronized(@NotNull String message, @NotNull final Throwable t) {
        validateAndFireSynchronized(TRACE, format(message, t), Set.of(t));
    }

    public void traceAsync(@NotNull String message, @NotNull final Throwable t) {
        validateAndFireAsync(TRACE, format(message, t), Set.of(t));
    }

    public void trace(@NotNull String message, @NotNull final EventException e) {
        validateAndFire(TRACE, format(message, e), Set.of(e));
    }

    public void traceSynchronized(@NotNull String message, @NotNull final EventException e) {
        validateAndFireSynchronized(TRACE, format(message, e), Set.of(e));
    }

    public void traceAsync(@NotNull String message, @NotNull final EventException e) {
        validateAndFireAsync(TRACE, format(message, e), Set.of(e));
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
        validateAndFire(TRACE, marker, message);
    }

    public void traceSynchronized(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFireSynchronized(TRACE, marker, message);
    }

    public void traceAsync(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFireAsync(TRACE, marker, message);
    }

    @Override
    public void trace(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFire(TRACE, marker, format(message,arg), Set.of(arg));
    }

    public void traceSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFireSynchronized(TRACE, marker, format(message,arg), Set.of(arg));
    }

    public void traceAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFireAsync(TRACE, marker, format(message,arg), Set.of(arg));
    }

    @Override
    public void trace(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFire(TRACE, marker, format(message,arg1,arg2), Set.of(arg1,arg2));
    }

    public void traceSynchronized(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFireSynchronized(TRACE, marker, format(message,arg1,arg2), Set.of(arg1,arg2));
    }

    public void traceAsync(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFireAsync(TRACE, marker, format(message,arg1,arg2), Set.of(arg1,arg2));
    }

    @Override
    public void trace(@NotNull final Marker marker, String message, Object... args) {
        validateAndFire(TRACE, marker, format(message,args), Set.of(args));
    }

    public void traceSynchronized(@NotNull final Marker marker, String message, Object... args) {
        validateAndFireSynchronized(TRACE, marker, format(message,args), Set.of(args));
    }

    public void traceAsync(@NotNull final Marker marker, String message, Object... args) {
        validateAndFireAsync(TRACE, marker, format(message,args), Set.of(args));
    }

    @Override
    public void trace(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        validateAndFire(TRACE, marker, format(message,t), Set.of(t));
    }

    public void traceSynchronized(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        validateAndFireSynchronized(TRACE, marker, format(message,t), Set.of(t));
    }

    public void traceAsync(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        validateAndFireAsync(TRACE, marker, format(message,t), Set.of(t));
    }

    public void trace(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        validateAndFire(TRACE, marker, format(message,e), Set.of(e));
    }

    public void traceSynchronized(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        validateAndFireSynchronized(TRACE, marker, format(message,e), Set.of(e));
    }

    public void traceAsync(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        validateAndFireAsync(TRACE, marker, format(message,e), Set.of(e));
    }

    /**
     * Check if log calls to Log Level {@link Level#DEBUG} will be logged or ignored.
     * @return True | False
     */
    @Contract(pure = true)
    @Override
    public boolean isDebugEnabled() {
        return isLoggable(DEBUG);
    }

    @Override
    public void debug(@NotNull final String message) {
        validateAndFire(DEBUG, message);
    }

    public void debugSynchronized(@NotNull final String message) {
        validateAndFireSynchronized(DEBUG, message);
    }

    public void debugAsync(@NotNull final String message) {
        validateAndFireAsync(DEBUG, message);
    }

    @Override
    public void debug(@NotNull String message, @NotNull final Object arg) {
        validateAndFire(DEBUG, format(message, arg), Set.of(arg));
    }

    public void debugSynchronized(@NotNull String message, @NotNull final Object arg) {
        validateAndFireSynchronized(DEBUG, format(message, arg), Set.of(arg));
    }

    public void debugAsync(@NotNull String message, @NotNull final Object arg) {
        validateAndFireAsync(DEBUG, format(message, arg), Set.of(arg));
    }

    @Override
    public void debug(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        validateAndFire(DEBUG, format(message, arg1, arg2), Set.of(arg1, arg2));
    }

    public void debugSynchronized(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        validateAndFireSynchronized(DEBUG, format(message, arg1, arg2), Set.of(arg1, arg2));
    }

    public void debugAsync(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        validateAndFireAsync(DEBUG, format(message, arg1, arg2), Set.of(arg1, arg2));
    }

    @Override
    public void debug(@NotNull String message, @NotNull final Object... args) {
        validateAndFire(DEBUG, format(message, args), Set.of(args));
    }

    public void debugSynchronized(@NotNull String message, @NotNull final Object... args) {
        validateAndFireSynchronized(DEBUG, format(message, args), Set.of(args));
    }

    public void debugAsync(@NotNull String message, @NotNull final Object... args) {
        validateAndFireAsync(DEBUG, format(message, args), Set.of(args));
    }

    @Override
    public void debug(@NotNull String message, @NotNull final Throwable t) {
        validateAndFire(DEBUG, format(message, t), Set.of(t));
    }

    public void debugSynchronized(@NotNull String message, @NotNull final Throwable t) {
        validateAndFireSynchronized(DEBUG, format(message, t), Set.of(t));
    }

    public void debugAsync(@NotNull String message, @NotNull final Throwable t) {
        validateAndFireAsync(DEBUG, format(message, t), Set.of(t));
    }

    public void debug(@NotNull String message, @NotNull final EventException e) {
        validateAndFire(DEBUG, format(message, e), Set.of(e));
    }

    public void debugSynchronized(@NotNull String message, @NotNull final EventException e) {
        validateAndFireSynchronized(DEBUG, format(message, e), Set.of(e));
    }

    public void debugAsync(@NotNull String message, @NotNull final EventException e) {
        validateAndFireAsync(DEBUG, format(message, e), Set.of(e));
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
        return isLoggable(DEBUG, marker);
    }

    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFire(DEBUG, marker, message);
    }

    public void debugSynchronized(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFireSynchronized(DEBUG, marker, message);
    }

    public void debugAsync(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFireAsync(DEBUG, marker, message);
    }

    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFire(DEBUG, marker, format(message,arg), Set.of(arg));
    }

    public void debugSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFireSynchronized(DEBUG, marker, format(message,arg), Set.of(arg));
    }

    public void debugAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFireAsync(DEBUG, marker, format(message,arg), Set.of(arg));
    }

    @Override
    public void debug(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFire(DEBUG, marker, format(message,arg1,arg2), Set.of(arg1,arg2));
    }

    public void debugSynchronized(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFireSynchronized(DEBUG, marker, format(message,arg1,arg2), Set.of(arg1,arg2));
    }

    public void debugAsync(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFireAsync(DEBUG, marker, format(message,arg1,arg2), Set.of(arg1,arg2));
    }

    @Override
    public void debug(@NotNull final Marker marker, String message, Object... args) {
        validateAndFire(DEBUG, marker, format(message,args), Set.of(args));
    }

    public void debugSynchronized(@NotNull final Marker marker, String message, Object... args) {
        validateAndFireSynchronized(DEBUG, marker, format(message,args), Set.of(args));
    }

    public void debugAsync(@NotNull final Marker marker, String message, Object... args) {
        validateAndFireAsync(DEBUG, marker, format(message,args), Set.of(args));
    }

    @Override
    public void debug(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        validateAndFire(DEBUG, marker, format(message,t), Set.of(t));
    }

    public void debugSynchronized(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        validateAndFireSynchronized(DEBUG, marker, format(message,t), Set.of(t));
    }

    public void debugAsync(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        validateAndFireAsync(DEBUG, marker, format(message,t), Set.of(t));
    }

    public void debug(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        validateAndFire(DEBUG, marker, format(message,e), Set.of(e));
    }

    public void debugSynchronized(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        validateAndFireSynchronized(DEBUG, marker, format(message,e), Set.of(e));
    }

    public void debugAsync(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        validateAndFireAsync(DEBUG, marker, format(message,e), Set.of(e));
    }

    /**
     * Check if log calls to Log Level {@link Level#INFO} will be logged or ignored.
     * @return True | False
     */
    @Contract(pure = true)
    @Override
    public boolean isInfoEnabled() {
        return isLoggable(INFO);
    }

    @Override
    public void info(@NotNull final String message) {
        validateAndFire(INFO, message);
    }

    public void infoSynchronized(@NotNull final String message) {
        validateAndFireSynchronized(INFO, message);
    }

    public void infoAsync(@NotNull final String message) {
        validateAndFireAsync(INFO, message);
    }

    @Override
    public void info(@NotNull String message, @NotNull final Object arg) {
        validateAndFire(INFO, format(message, arg), Set.of(arg));
    }

    public void infoSynchronized(@NotNull String message, @NotNull final Object arg) {
        validateAndFireSynchronized(INFO, format(message, arg), Set.of(arg));
    }

    public void infoAsync(@NotNull String message, @NotNull final Object arg) {
        validateAndFireAsync(INFO, format(message, arg), Set.of(arg));
    }

    @Override
    public void info(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        validateAndFire(INFO, format(message, arg1, arg2), Set.of(arg1, arg2));
    }

    public void infoSynchronized(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        validateAndFireSynchronized(INFO, format(message, arg1, arg2), Set.of(arg1, arg2));
    }

    public void infoAsync(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        validateAndFireAsync(INFO, format(message, arg1, arg2), Set.of(arg1, arg2));
    }

    @Override
    public void info(@NotNull String message, @NotNull final Object... args) {
        validateAndFire(INFO, format(message, args), Set.of(args));
    }

    public void infoSynchronized(@NotNull String message, @NotNull final Object... args) {
        validateAndFireSynchronized(INFO, format(message, args), Set.of(args));
    }

    public void infoAsync(@NotNull String message, @NotNull final Object... args) {
        validateAndFireAsync(INFO, format(message, args), Set.of(args));
    }

    @Override
    public void info(@NotNull String message, @NotNull final Throwable t) {
        validateAndFire(INFO, format(message, t), Set.of(t));
    }

    public void infoSynchronized(@NotNull String message, @NotNull final Throwable t) {
        validateAndFireSynchronized(INFO, format(message, t), Set.of(t));
    }

    public void infoAsync(@NotNull String message, @NotNull final Throwable t) {
        validateAndFireAsync(INFO, format(message, t), Set.of(t));
    }

    public void info(@NotNull String message, @NotNull final EventException e) {
        validateAndFire(INFO, format(message, e), Set.of(e));
    }

    public void infoSynchronized(@NotNull String message, @NotNull final EventException e) {
        validateAndFireSynchronized(INFO, format(message, e), Set.of(e));
    }

    public void infoAsync(@NotNull String message, @NotNull final EventException e) {
        validateAndFireAsync(INFO, format(message, e), Set.of(e));
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
        return isLoggable(INFO, marker);
    }

    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFire(INFO, marker, message);
    }

    public void infoSynchronized(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFireSynchronized(INFO, marker, message);
    }

    public void infoAsync(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFireAsync(INFO, marker, message);
    }

    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFire(INFO, marker, format(message,arg), Set.of(arg));
    }

    public void infoSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFireSynchronized(INFO, marker, format(message,arg), Set.of(arg));
    }

    public void infoAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFireAsync(INFO, marker, format(message,arg), Set.of(arg));
    }

    @Override
    public void info(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFire(INFO, marker, format(message,arg1,arg2), Set.of(arg1,arg2));
    }

    public void infoSynchronized(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFireSynchronized(INFO, marker, format(message,arg1,arg2), Set.of(arg1,arg2));
    }

    public void infoAsync(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFireAsync(INFO, marker, format(message,arg1,arg2), Set.of(arg1,arg2));
    }

    @Override
    public void info(@NotNull final Marker marker, String message, Object... args) {
        validateAndFire(INFO, marker, format(message,args), Set.of(args));
    }

    public void infoSynchronized(@NotNull final Marker marker, String message, Object... args) {
        validateAndFireSynchronized(INFO, marker, format(message,args), Set.of(args));
    }

    public void infoAsync(@NotNull final Marker marker, String message, Object... args) {
        validateAndFireAsync(INFO, marker, format(message,args), Set.of(args));
    }

    @Override
    public void info(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        validateAndFire(INFO, marker, format(message,t), Set.of(t));
    }

    public void infoSynchronized(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        validateAndFireSynchronized(INFO, marker, format(message,t), Set.of(t));
    }

    public void infoAsync(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        validateAndFireAsync(INFO, marker, format(message,t), Set.of(t));
    }

    public void info(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        validateAndFire(INFO, marker, format(message,e), Set.of(e));
    }

    public void infoSynchronized(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        validateAndFireSynchronized(INFO, marker, format(message,e), Set.of(e));
    }

    public void infoAsync(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        validateAndFireAsync(INFO, marker, format(message,e), Set.of(e));
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
        validateAndFire(WARNING, message);
    }

    public void warnSynchronized(@NotNull final String message) {
        validateAndFireSynchronized(WARNING, message);
    }

    public void warnAsync(@NotNull final String message) {
        validateAndFireAsync(WARNING, message);
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
        validateAndFire(WARNING, format(message, arg), Set.of(arg));
    }

    public void warnSynchronized(@NotNull final String message, @NotNull final Object arg) {
        validateAndFireSynchronized(WARNING, format(message, arg), Set.of(arg));
    }

    public void warnAsync(@NotNull final String message, @NotNull final Object arg) {
        validateAndFireAsync(WARNING, format(message, arg), Set.of(arg));
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
        validateAndFire(WARNING, format(message, args), Set.of(args));
    }

    public void warnSynchronized(@NotNull final String message, @NotNull final Object... args) {
        validateAndFireSynchronized(WARNING, format(message, args), Set.of(args));
    }

    public void warnAsync(@NotNull final String message, @NotNull final Object... args) {
        validateAndFireAsync(WARNING, format(message, args), Set.of(args));
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
        validateAndFire(WARNING, format(message, arg1,arg2), Set.of(arg1,arg2));
    }

    public void warnSynchronized(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFireSynchronized(WARNING, format(message, arg1,arg2), Set.of(arg1,arg2));
    }

    public void warnAsync(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFireAsync(WARNING, format(message, arg1,arg2), Set.of(arg1,arg2));
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
        validateAndFire(WARNING, format(message, t), Set.of(t));
    }

    public void warnSynchronized(@NotNull final String message, @NotNull final Throwable t) {
        validateAndFireSynchronized(WARNING, format(message, t), Set.of(t));
    }

    public void warnAsync(@NotNull final String message, @NotNull final Throwable t) {
        validateAndFireAsync(WARNING, format(message, t), Set.of(t));
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
        validateAndFire(WARNING, format(message, e), Set.of(e));
    }

    public void warnSynchronized(@NotNull final String message, @NotNull final EventException e) {
        validateAndFireSynchronized(WARNING, format(message, e), Set.of(e));
    }

    public void warnAsync(@NotNull final String message, @NotNull final EventException e) {
        validateAndFireAsync(WARNING, format(message, e), Set.of(e));
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
        validateAndFire(WARNING, marker, message);
    }

    public void warnSynchronized(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFireSynchronized(WARNING, marker, message);
    }

    public void warnAsync(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFireAsync(WARNING, marker, message);
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
        validateAndFire(WARNING, marker, format(message, arg), Set.of(arg));
    }

    public void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFireSynchronized(WARNING, marker, format(message, arg), Set.of(arg));
    }

    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFireAsync(WARNING, marker, format(message, arg), Set.of(arg));
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
        validateAndFire(WARNING, marker, format(message, arg1,arg2), Set.of(arg1,arg2));
    }

    public void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFireSynchronized(WARNING, marker, format(message, arg1,arg2), Set.of(arg1,arg2));
    }

    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFireAsync(WARNING, marker, format(message, arg1,arg2), Set.of(arg1,arg2));
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
        validateAndFire(WARNING, marker, format(message, args), Set.of(args));
    }

    public void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        validateAndFireSynchronized(WARNING, marker, format(message, args), Set.of(args));
    }

    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        validateAndFireAsync(WARNING, marker, format(message, args), Set.of(args));
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
        validateAndFire(WARNING, marker, format(message, t), Set.of(t));
    }

    public void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        validateAndFireSynchronized(WARNING, marker, format(message, t), Set.of(t));
    }

    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        validateAndFireAsync(WARNING, marker, format(message, t), Set.of(t));
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
        validateAndFire(WARNING, marker, format(message, e), Set.of(e));
    }

    public void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        validateAndFireSynchronized(WARNING, marker, format(message, e), Set.of(e));
    }

    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final EventException e) {
        validateAndFireAsync(WARNING, marker, format(message, e), Set.of(e));
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
     * @return True | False
     */
    @Contract(pure = true)
    @Override
    public boolean isErrorEnabled() {
        return isLoggable(ERROR);
    }

    @Override
    public void error(@NotNull final String message) {
        validateAndFire(ERROR, message);
    }

    public void errorSynchronized(@NotNull final String message) {
        validateAndFireSynchronized(ERROR, message);
    }

    public void errorAsync(@NotNull final String message) {
        validateAndFireAsync(ERROR, message);
    }

    @Override
    public void error(@NotNull String message, @NotNull final Object arg) {
        validateAndFire(ERROR, format(message, arg), Set.of(arg));
    }

    public void errorSynchronized(@NotNull String message, @NotNull final Object arg) {
        validateAndFireSynchronized(ERROR, format(message, arg), Set.of(arg));
    }

    public void errorAsync(@NotNull String message, @NotNull final Object arg) {
        validateAndFireAsync(ERROR, format(message, arg), Set.of(arg));
    }

    @Override
    public void error(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        validateAndFire(ERROR, format(message, arg1, arg2), Set.of(arg1, arg2));
    }

    public void errorSynchronized(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        validateAndFireSynchronized(ERROR, format(message, arg1, arg2), Set.of(arg1, arg2));
    }

    public void errorAsync(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        validateAndFireAsync(ERROR, format(message, arg1, arg2), Set.of(arg1, arg2));
    }

    @Override
    public void error(@NotNull String message, @NotNull final Object... args) {
        validateAndFire(ERROR, format(message, args), Set.of(args));
    }

    public void errorSynchronized(@NotNull String message, @NotNull final Object... args) {
        validateAndFireSynchronized(ERROR, format(message, args), Set.of(args));
    }

    public void errorAsync(@NotNull String message, @NotNull final Object... args) {
        validateAndFireAsync(ERROR, format(message, args), Set.of(args));
    }

    @Override
    public void error(@NotNull String message, @NotNull final Throwable t) {
        validateAndFire(ERROR, format(message, t), Set.of(t));
    }

    public void errorSynchronized(@NotNull String message, @NotNull final Throwable t) {
        validateAndFireSynchronized(ERROR, format(message, t), Set.of(t));
    }

    public void errorAsync(@NotNull String message, @NotNull final Throwable t) {
        validateAndFireAsync(ERROR, format(message, t), Set.of(t));
    }

    public void error(@NotNull String message, @NotNull final EventException e) {
        validateAndFire(ERROR, format(message, e), Set.of(e));
    }

    public void errorSynchronized(@NotNull String message, @NotNull final EventException e) {
        validateAndFireSynchronized(ERROR, format(message, e), Set.of(e));
    }

    public void errorAsync(@NotNull String message, @NotNull final EventException e) {
        validateAndFireAsync(ERROR, format(message, e), Set.of(e));
    }

    /**
     * Check if log calls to Log Level {@link Level#ERROR} with the given {@link Marker} will be logged or ignored.
     * @return True, if the current Log Level is of Level Error, or if the Marker is a registered forced marker.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Contract(pure = true)
    @Override
    public boolean isErrorEnabled(@NotNull final Marker marker) {
        return isLoggable(ERROR, marker);
    }

    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFire(ERROR, marker, message);
    }

    public void errorSynchronized(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFireSynchronized(ERROR, marker, message);
    }

    public void errorAsync(@NotNull final Marker marker, @NotNull final String message) {
        validateAndFireAsync(ERROR, marker, message);
    }

    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFire(ERROR, marker, format(message,arg), Set.of(arg));
    }

    public void errorSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFireSynchronized(ERROR, marker, format(message,arg), Set.of(arg));
    }

    public void errorAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        validateAndFireAsync(ERROR, marker, format(message,arg), Set.of(arg));
    }

    @Override
    public void error(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFire(ERROR, marker, format(message,arg1,arg2), Set.of(arg1,arg2));
    }

    public void errorSynchronized(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFireSynchronized(ERROR, marker, format(message,arg1,arg2), Set.of(arg1,arg2));
    }

    public void errorAsync(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        validateAndFireAsync(ERROR, marker, format(message,arg1,arg2), Set.of(arg1,arg2));
    }

    @Override
    public void error(@NotNull final Marker marker, String message, Object... args) {
        validateAndFire(ERROR, marker, format(message,args), Set.of(args));
    }

    public void errorSynchronized(@NotNull final Marker marker, String message, Object... args) {
        validateAndFireSynchronized(ERROR, marker, format(message,args), Set.of(args));
    }

    public void errorAsync(@NotNull final Marker marker, String message, Object... args) {
        validateAndFireAsync(ERROR, marker, format(message,args), Set.of(args));
    }

    @Override
    public void error(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        validateAndFire(ERROR, marker, format(message,t), Set.of(t));
    }

    public void errorSynchronized(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        validateAndFireSynchronized(ERROR, marker, format(message,t), Set.of(t));
    }

    public void errorAsync(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        validateAndFireAsync(ERROR, marker, format(message,t), Set.of(t));
    }

    public void error(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        validateAndFire(ERROR, marker, format(message,e), Set.of(e));
    }

    public void errorSynchronized(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        validateAndFireSynchronized(ERROR, marker, format(message,e), Set.of(e));
    }

    public void errorAsync(@NotNull final Marker marker, String message, @NotNull final EventException e) {
        validateAndFireAsync(ERROR, marker, format(message,e), Set.of(e));
    }

    @Contract(pure = true)
    private static @NotNull String format(@NotNull final String log, @NotNull final Object... args) {
        String message = log.replaceAll("%[s|d]", Matcher.quoteReplacement("{}"))
                .replace("%d", Matcher.quoteReplacement("{}"))
                .replaceAll("(%(\\.\\d)?f)", Matcher.quoteReplacement("{}"));
        for (Object arg : args) {
            if (arg instanceof EventException e) arg = e.getCause();
            switch (arg) {
                case ErrorResponseException error -> {
                    message = message.replaceFirst("(?<!\\n)(\\{})", Matcher.quoteReplacement("\n{}"));
                    final StringBuilder builder = new StringBuilder();
                    builder.append("\n").append(EXCEPTION.getColor()).append("[")
                            .append(error.getErrorCode()).append("]:")
                            .append(error.getErrorResponse()).append(" - ")
                            .append(error.getMeaning()).append("\n");
                    for (final StackTraceElement st : error.getStackTrace()) {
                        builder.append(Level.EXCEPTION.getColor()).append(st.toString()).append("\n");
                    }
                    message = message.replaceFirst("\\{}", Matcher.quoteReplacement(builder.toString()));
                }
                case Throwable t -> {
                    final StringBuilder builder = new StringBuilder();
                    builder.append(Level.EXCEPTION.getColor()).append(t.getMessage()).append("\n");
                    for (final StackTraceElement st : t.getStackTrace()) {
                        builder.append(Level.EXCEPTION.getColor()).append(st.toString()).append("\n");
                    }
                    message = message.replaceFirst("\\{}", Matcher.quoteReplacement(builder.toString()));
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