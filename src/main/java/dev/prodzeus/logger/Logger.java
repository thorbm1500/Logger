package dev.prodzeus.logger;

import dev.prodzeus.logger.components.Level;
import dev.prodzeus.logger.internal.EventManager;
import dev.prodzeus.logger.internal.Formatter;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.prodzeus.logger.components.Level.*;

/**
 * Logger class.
 */
public final class Logger implements org.slf4j.Logger {

    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final String name;
    private Level level = INFO;
    private boolean ignoreGlobalLevel = false;

    private static final Set<Marker> forcedMarkers = Collections.synchronizedSet(new HashSet<>());

    /**
     * @apiNote <b>For internal use only.</b>
     */
    @Contract(pure = true)
    Logger(@NotNull final String name) {
        this.name = name;
        process(false,level,null,null,"@greenNew Logger instance created.",Collections.emptySet());
    }

    /**
     * Registers a Forced {@link Marker}.
     * Forced Markers are static and thus shared across all Loggers.
     * @param marker Marker to register.
     * @return       The Logger instance.
     * @apiNote      A log with a forced Marker will <b>always</b> be logged, regardless of the current Global and individual {@link Level}.
     */
    public @NotNull Logger registerForcedMarker(@NotNull final Marker marker) {
        forcedMarkers.add(marker);
        return this;
    }

    /**
     * Unregisters a Forced {@link Marker}.
     * @param marker Marker to unregister.
     * @return       The Logger instance.
     */
    public @NotNull Logger unregisterForcedMarker(@NotNull final Marker marker) {
        forcedMarkers.remove(marker);
        return this;
    }

    /**
     * Clears all registered Forced {@link Marker}s.
     * @return The Logger instance.
     */
    public @NotNull Logger clearForcedMarkers() {
        forcedMarkers.clear();
        return this;
    }

    /**
     * This setting dictates whether the Global level should be respected or ignored.
     * <b>Default:</b> {@code False}
     * @param ignore True | False
     * @see SLF4JProvider#setGlobalLevel(Level) SLF4JProvider#setGlobalLevel
     */
    public void ignoreGlobalLogLevel(final boolean ignore) {
        this.ignoreGlobalLevel = ignore;
    }

    /**
     * Sets the current log level for this instance. Any log call below this level will be ignored.
     * @param level New log level.
     * @return The Logger instance.
     * @see SLF4JProvider#setGlobalLevel(Level) SLF4JProvider#setGlobalLevel
     */
    public @NotNull Logger setLevel(@NotNull final Level level) {
        this.level = level;
        return this;
    }

    /**
     * Gets the current log level set.
     * Any logs logged below this level will be ignored unless a registered Forced Marker is attached to the log.
     * @return The Logger's current level.
     * @see Logger#registerForcedMarker(Marker) Logger#registerForcedMarker
     */
    @Contract(pure = true)
    public @NotNull Level getLevel() {
        return level;
    }

    /**
     * Gets the name of the instance.
     * @return The Logger's name.
     */
    @Override @Contract(pure = true)
    public @NotNull String getName() {
        return name;
    }

    /**
     * Checks if a log to the specified level would be logged or ignored.
     * @param level The log level.
     * @return True, if logs at this level are logged, otherwise false.
     */
    @Contract(pure = true)
    public boolean isLoggable(@NotNull final Level level) {
        return ((ignoreGlobalLevel ? 0 : SLF4JProvider.getGlobalLevel().getWeight()) & this.level.getWeight()) <= level.getWeight();
    }

    /**
     * Checks if a log to the specified level with the specified Marker would be logged or ignored.
     * @param marker The Marker.
     * @param level  The level.
     * @return       True, if logs at this level are logged,
     *               or if the marker is a registered Forced Marker, otherwise false.
     * @see          Logger#registerForcedMarker(Marker) Logger#registerForcedMarker
     */
    @Contract(pure = true)
    public boolean isLoggable(@NotNull final Level level, @Nullable final Marker marker) {
        if (marker != null && forcedMarkers.contains(marker)) return true;
        return isLoggable(level);
    }

    /**
     * @apiNote <b>For internal use only.</b>
     */
    private void process(@NotNull Level level, @NotNull String message) {
        process(level, null, null, message, Collections.emptySet());
    }

    /**
     * @apiNote <b>For internal use only.</b>
     */
    private void process(@NotNull Level level, @NotNull Throwable throwable) {
        process(level, null, throwable, "", Collections.emptySet());
    }

    /**
     * @apiNote <b>For internal use only.</b>
     */
    private void process(@NotNull Level level, @NotNull String message, @NotNull final Object... args) {
        process(level, null, null, message, List.of(args));
    }

    /**
     * @apiNote <b>For internal use only.</b>
     */
    private void process(@NotNull Level level, @Nullable final Marker marker, @NotNull String message) {
        process(level, marker, null, message, Collections.emptySet());
    }

    /**
     * @apiNote <b>For internal use only.</b>
     */
    private void process(@NotNull Level level, @Nullable final Marker marker, @NotNull Throwable throwable) {
        process(level, marker, throwable, "", Collections.emptySet());
    }

    /**
     * @apiNote <b>For internal use only.</b>
     */
    private void process(@NotNull Level level, @Nullable final Marker marker, @NotNull String message, @NotNull final Object... args) {
        process(level, marker, null, message, Set.of(args));
    }

    /**
     * @apiNote <b>For internal use only.</b>
     */
    private void process(@NotNull Level level, @Nullable final Marker marker, @Nullable final Throwable throwable, @NotNull String message, @NotNull final Object... args) {
        process(level, marker, throwable, message, List.of(args));
    }

    /**
     * @apiNote <b>For internal use only.</b>
     */
    private void process(@NotNull Level level, @Nullable final Marker marker, @Nullable final Throwable throwable, @NotNull String message, @NotNull final Collection<Object> args) {
        process(true,level,marker,throwable,message,args);
    }

    /**
     * @apiNote <b>For internal use only.</b>
     */
    private void process(final boolean fireEvent, @NotNull Level level, @Nullable final Marker marker, @Nullable final Throwable throwable, @NotNull String message, @NotNull final Collection<Object> args) {
        final List<Object> arguments = new ArrayList<>(args);
        if (throwable != null) arguments.add(throwable);
        final Pair<String, Level> formatted = Formatter.formatPlaceholders(message,arguments);
        if (formatted.getRight() == EXCEPTION) {
            level = EXCEPTION;
            System.out.flush();
        }
        final String rawMessage = formatted.getLeft();
        message = Formatter.constructLogMessage(this, level, marker, rawMessage, true);
        if (fireEvent) EventManager.handleEvent(new Event(this,level,marker,throwable,rawMessage,message,arguments));
        if (isLoggable(level)) System.out.println(message);
    }

    /**
     * Checks if logs at {@link Level#TRACE TRACE} level are logged or ignored.<br>
     * No exceptions will be thrown if the format is not followed; the arguments will simply not be added to the log.
     * The expected format of placeholders;
     *      <pre>
     *          {@code logger.trace("Example {}.", Object);}
     *      </pre>
     * @return True | False
     */
    @Override @Contract(pure = true)
    public boolean isTraceEnabled() {
        return isLoggable(Level.TRACE);
    }

    /**
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    @Override
    public void trace(@NotNull final String message) {
         process(TRACE,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public synchronized void traceSynchronized(@NotNull final String message) {
        trace(message);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void traceAsync(@NotNull final String message) {
        executor.submit(() -> trace(message));
    }

    /**
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    @Override
    public void trace(@NotNull final String message, final Object arg) {
        process(TRACE,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public synchronized void traceSynchronized(@NotNull final String message, final Object arg) {
        trace(message,arg);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void traceAsync(@NotNull final String message, final Object arg) {
        executor.submit(() -> trace(message,arg));
    }

    /**
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    @Override
    public void trace(@NotNull final String message, final Object arg1, final Object arg2) {
        process(TRACE,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public synchronized void traceSynchronized(@NotNull final String message, final Object arg1, final Object arg2) {
        trace(message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void traceAsync(@NotNull final String message, final Object arg1, final Object arg2) {
        executor.submit(() -> trace(message,arg1,arg2));
    }

    /**
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    @Override
    public void trace(@NotNull final String message, Object... args) {
        process(TRACE,message,args);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public synchronized void traceSynchronized(@NotNull final String message, Object... args) {
        trace(message,args);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void traceAsync(@NotNull final String message, Object... args) {
        executor.submit(() -> trace(message,args));
    }

    /**
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    @Override
    public void trace(@NotNull final String message, @NotNull final Throwable t) {
        process(TRACE,message,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public synchronized void traceSynchronized(@NotNull final String message, @NotNull final Throwable t) {
        trace(message,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void traceAsync(@NotNull final String message, @NotNull final Throwable t) {
        executor.submit(() -> trace(message,t));
    }

    /**
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void trace(@NotNull final Throwable t) {
        process(TRACE,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public synchronized void traceSynchronized(@NotNull final Throwable t) {
        trace(t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void traceAsync(@NotNull final Throwable t) {
        executor.submit(() -> trace(t));
    }

    /**
     * Checks if logs at {@link Level#TRACE TRACE} level, with the specified Marker, are logged or ignored.<br>
     * No exceptions will be thrown if the format is not followed; the arguments will simply not be added to the log.
     * The expected format of placeholders;
     *      <pre>
     *          {@code logger.trace("Example {}.", Object);}
     *      </pre>
     * @return True | False
     */
    @Override @Contract(pure = true)
    public boolean isTraceEnabled(@NotNull final Marker marker) {
        return isLoggable(Level.TRACE, marker);
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    @Override
    public void trace(@Nullable final Marker marker, @NotNull final String message) {
        process(TRACE,marker,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public synchronized void traceSynchronized(@Nullable final Marker marker, @NotNull final String message) {
        trace(marker,message);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void traceAsync(@Nullable final Marker marker, @NotNull final String message) {
        executor.submit(() -> trace(marker,message));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    @Override
    public void trace(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        process(TRACE,marker,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public synchronized void traceSynchronized(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        trace(marker,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void traceAsync(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        executor.submit(() -> trace(marker,message,arg));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    @Override
    public void trace(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        process(TRACE,marker,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public synchronized void traceSynchronized(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        trace(marker,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void traceAsync(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        executor.submit(() -> trace(marker,message,arg1,arg2));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    @Override
    public void trace(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        process(TRACE,marker,message,args);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public synchronized void traceSynchronized(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        trace(marker,message,args);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void traceAsync(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        executor.submit(() -> trace(marker,message,args));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    @Override
    public void trace(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        process(TRACE,marker,t,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public synchronized void traceSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        trace(marker,message,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void traceAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        executor.submit(() -> trace(marker,message,t));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void trace(@NotNull final Marker marker, @NotNull final Throwable t) {
        process(TRACE,marker,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public synchronized void traceSynchronized(@NotNull final Marker marker, @NotNull final Throwable t) {
        trace(marker,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#TRACE TRACE} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isTraceEnabled() Logger#isTraceEnabled
     */
    public void traceAsync(@NotNull final Marker marker, @NotNull final Throwable t) {
        executor.submit(() -> trace(marker,t));
    }

    /**
     * Checks if logs at {@link Level#DEBUG DEBUG} level are logged or ignored.<br>
     * No exceptions will be thrown if the format is not followed; the arguments will simply not be added to the log.
     * The expected format of placeholders;
     *      <pre>
     *          {@code logger.debug("Example {}.", Object);}
     *      </pre>
     * @return True | False
     */
    @Override @Contract(pure = true)
    public boolean isDebugEnabled() {
        return isLoggable(DEBUG);
    }

    /**
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    @Override
    public void debug(@NotNull final String message) {
        process(DEBUG,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public synchronized void debugSynchronized(@NotNull final String message) {
        debug(message);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debugAsync(@NotNull final String message) {
        executor.submit(() -> debug(message));
    }

    /**
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    @Override
    public void debug(@NotNull final String message, final Object arg) {
        process(DEBUG,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public synchronized void debugSynchronized(@NotNull final String message, final Object arg) {
        debug(message,arg);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debugAsync(@NotNull final String message, final Object arg) {
        executor.submit(() -> debug(message,arg));
    }

    /**
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    @Override
    public void debug(@NotNull final String message, final Object arg1, final Object arg2) {
        process(DEBUG,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public synchronized void debugSynchronized(@NotNull final String message, final Object arg1, final Object arg2) {
        debug(message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debugAsync(@NotNull final String message, final Object arg1, final Object arg2) {
        executor.submit(() -> debug(message,arg1,arg2));
    }

    /**
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    @Override
    public void debug(@NotNull final String message, Object... args) {
        process(DEBUG,message,args);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public synchronized void debugSynchronized(@NotNull final String message, Object... args) {
        debug(message,args);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debugAsync(@NotNull final String message, Object... args) {
        executor.submit(() -> debug(message,args));
    }

    /**
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    @Override
    public void debug(@NotNull final String message, @NotNull final Throwable t) {
        process(DEBUG,message,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public synchronized void debugSynchronized(@NotNull final String message, @NotNull final Throwable t) {
        debug(message,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debugAsync(@NotNull final String message, @NotNull final Throwable t) {
        executor.submit(() -> debug(message,t));
    }

    /**
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debug(@NotNull final Throwable t) {
        process(DEBUG,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public synchronized void debugSynchronized(@NotNull final Throwable t) {
        debug(t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debugAsync(@NotNull final Throwable t) {
        executor.submit(() -> debug(t));
    }

    /**
     * Checks if logs at {@link Level#DEBUG DEBUG} level, with the specified Marker, are logged or ignored.<br>
     * No exceptions will be thrown if the format is not followed; the arguments will simply not be added to the log.
     * The expected format of placeholders;
     *      <pre>
     *          {@code logger.debug("Example {}.", Object);}
     *      </pre>
     * @return True | False
     */
    @Override @Contract(pure = true)
    public boolean isDebugEnabled(@NotNull final Marker marker) {
        return isLoggable(Level.DEBUG, marker);
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    @Override
    public void debug(@Nullable final Marker marker, @NotNull final String message) {
        process(DEBUG,marker,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public synchronized void debugSynchronized(@Nullable final Marker marker, @NotNull final String message) {
        debug(marker,message);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debugAsync(@Nullable final Marker marker, @NotNull final String message) {
        executor.submit(() -> debug(marker,message));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        process(DEBUG,marker,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public synchronized void debugSynchronized(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        debug(marker,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debugAsync(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        executor.submit(() -> debug(marker,message,arg));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        process(DEBUG,marker,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public synchronized void debugSynchronized(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        debug(marker,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debugAsync(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        executor.submit(() -> debug(marker,message,arg1,arg2));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        process(DEBUG,marker,message,args);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public synchronized void debugSynchronized(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        debug(marker,message,args);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debugAsync(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        executor.submit(() -> debug(marker,message,args));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        process(DEBUG,marker,t,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public synchronized void debugSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        debug(marker,message,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debugAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        executor.submit(() -> debug(marker,message,t));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debug(@NotNull final Marker marker, @NotNull final Throwable t) {
        process(DEBUG,marker,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public synchronized void debugSynchronized(@NotNull final Marker marker, @NotNull final Throwable t) {
        debug(marker,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#DEBUG DEBUG} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isDebugEnabled() Logger#isDebugEnabled
     */
    public void debugAsync(@NotNull final Marker marker, @NotNull final Throwable t) {
        executor.submit(() -> debug(marker,t));
    }

    /**
     * Checks if logs at {@link Level#INFO INFO} level are logged or ignored.<br>
     * No exceptions will be thrown if the format is not followed; the arguments will simply not be added to the log.
     * The expected format of placeholders;
     *      <pre>
     *          {@code logger.info("Example {}.", Object);}
     *      </pre>
     * @return True | False
     */
    @Override @Contract(pure = true)
    public boolean isInfoEnabled() {
        return isLoggable(Level.INFO);
    }

    /**
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    @Override
    public void info(@NotNull final String message) {
        process(INFO,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public synchronized void infoSynchronized(@NotNull final String message) {
        info(message);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void infoAsync(@NotNull final String message) {
        executor.submit(() -> info(message));
    }

    /**
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    @Override
    public void info(@NotNull final String message, final Object arg) {
        process(INFO,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public synchronized void infoSynchronized(@NotNull final String message, final Object arg) {
        info(message,arg);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void infoAsync(@NotNull final String message, final Object arg) {
        executor.submit(() -> info(message,arg));
    }

    /**
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    @Override
    public void info(@NotNull final String message, final Object arg1, final Object arg2) {
        process(INFO,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public synchronized void infoSynchronized(@NotNull final String message, final Object arg1, final Object arg2) {
        info(message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void infoAsync(@NotNull final String message, final Object arg1, final Object arg2) {
        executor.submit(() -> info(message,arg1,arg2));
    }

    /**
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    @Override
    public void info(@NotNull final String message, Object... args) {
        process(INFO,message,args);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public synchronized void infoSynchronized(@NotNull final String message, Object... args) {
        info(message,args);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void infoAsync(@NotNull final String message, Object... args) {
        executor.submit(() -> info(message,args));
    }

    /**
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    @Override
    public void info(@NotNull final String message, @NotNull final Throwable t) {
        process(INFO,message,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public synchronized void infoSynchronized(@NotNull final String message, @NotNull final Throwable t) {
        info(message,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void infoAsync(@NotNull final String message, @NotNull final Throwable t) {
        executor.submit(() -> info(message,t));
    }

    /**
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void info(@NotNull final Throwable t) {
        process(INFO,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public synchronized void infoSynchronized(@NotNull final Throwable t) {
        info(t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void infoAsync(@NotNull final Throwable t) {
        executor.submit(() -> info(t));
    }

    /**
     * Checks if logs at {@link Level#INFO INFO} level, with the specified Marker, are logged or ignored.<br>
     *     No exceptions will be thrown if the format is not followed; the arguments will simply not be added to the log.
     *     The expected format of placeholders;
     *      <pre>
     *          {@code logger.info("Example {}.", Object);}
     *      </pre>
     * @return True | False
     */
    @Override @Contract(pure = true)
    public boolean isInfoEnabled(@NotNull final Marker marker) {
        return isLoggable(Level.INFO, marker);
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    @Override
    public void info(@Nullable final Marker marker, @NotNull final String message) {
        process(INFO,marker,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public synchronized void infoSynchronized(@Nullable final Marker marker, @NotNull final String message) {
        info(marker,message);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void infoAsync(@Nullable final Marker marker, @NotNull final String message) {
        executor.submit(() -> info(marker,message));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        process(INFO,marker,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public synchronized void infoSynchronized(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        info(marker,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void infoAsync(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        executor.submit(() -> info(marker,message,arg));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        process(INFO,marker,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public synchronized void infoSynchronized(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        info(marker,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void infoAsync(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        executor.submit(() -> info(marker,message,arg1,arg2));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        process(INFO,marker,message,args);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public synchronized void infoSynchronized(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        info(marker,message,args);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void infoAsync(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        executor.submit(() -> info(marker,message,args));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        process(INFO,marker,t,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public synchronized void infoSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        info(marker,message,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void infoAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        executor.submit(() -> info(marker,message,t));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void info(@NotNull final Marker marker, @NotNull final Throwable t) {
        process(INFO,marker,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public synchronized void infoSynchronized(@NotNull final Marker marker, @NotNull final Throwable t) {
        info(marker,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#INFO INFO} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isInfoEnabled() Logger#isInfoEnabled
     */
    public void infoAsync(@NotNull final Marker marker, @NotNull final Throwable t) {
        executor.submit(() -> info(marker,t));
    }

    /**
     * Checks if logs at {@link Level#WARNING WARNING} level are logged or ignored.<br>
     *     No exceptions will be thrown if the format is not followed; the arguments will simply not be added to the log.
     *     The expected format of placeholders;
     *      <pre>
     *          {@code logger.warn("Example {}.", Object);}
     *      </pre>
     * @return True | False
     */
    @Override @Contract(pure = true)
    public boolean isWarnEnabled() {
        return isLoggable(Level.WARNING);
    }

    /**
     * Checks if logs at {@link Level#WARNING WARNING} level are logged or ignored.<br>
     *     No exceptions will be thrown if the format is not followed; the arguments will simply not be added to the log.
     *     The expected format of placeholders;
     *      <pre>
     *          {@code logger.warn("Example {}.", Object);}
     *      </pre>
     * @return True | False
     */
    @Contract(pure = true)
    public boolean isWarningEnabled() {
        return isLoggable(Level.WARNING);
    }

    /**
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    @Override
    public void warn(@NotNull final String message) {
        process(WARNING,message);
    }

    /**
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warning(@NotNull final String message) {
        warn(message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warnSynchronized(@NotNull final String message) {
        warn(message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warningSynchronized(@NotNull final String message) {
        warn(message);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warnAsync(@NotNull final String message) {
        executor.submit(() -> warn(message));
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warningAsync(@NotNull final String message) {
        executor.submit(() -> warn(message));
    }

    /**
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    @Override
    public void warn(@NotNull final String message, final Object arg) {
        process(WARNING,message,arg);
    }

    /**
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warning(@NotNull final String message, final Object arg) {
        warn(message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warnSynchronized(@NotNull final String message, final Object arg) {
        warn(message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warningSynchronized(@NotNull final String message, final Object arg) {
        warn(message,arg);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warnAsync(@NotNull final String message, final Object arg) {
        executor.submit(() -> warn(message,arg));
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warningAsync(@NotNull final String message, final Object arg) {
        executor.submit(() -> warn(message,arg));
    }

    /**
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    @Override
    public void warn(@NotNull final String message, final Object arg1, final Object arg2) {
        process(WARNING,message,arg1,arg2);
    }

    /**
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warning(@NotNull final String message, final Object arg1, final Object arg2) {
        warn(message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warnSynchronized(@NotNull final String message, final Object arg1, final Object arg2) {
        warn(message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warningSynchronized(@NotNull final String message, final Object arg1, final Object arg2) {
        warn(message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warnAsync(@NotNull final String message, final Object arg1, final Object arg2) {
        executor.submit(() -> warn(message,arg1,arg2));
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warningAsync(@NotNull final String message, final Object arg1, final Object arg2) {
        executor.submit(() -> warn(message,arg1,arg2));
    }

    /**
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    @Override
    public void warn(@NotNull final String message, Object... args) {
        process(WARNING,message,args);
    }

    /**
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warning(@NotNull final String message, Object... args) {
        warn(message,args);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warnSynchronized(@NotNull final String message, Object... args) {
        warn(message,args);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warningSynchronized(@NotNull final String message, Object... args) {
        warn(message,args);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warnAsync(@NotNull final String message, Object... args) {
        executor.submit(() -> warn(message,args));
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warningAsync(@NotNull final String message, Object... args) {
        executor.submit(() -> warn(message,args));
    }

    /**
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    @Override
    public void warn(@NotNull final String message, @NotNull final Throwable t) {
        process(WARNING,message,t);
    }

    /**
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warning(@NotNull final String message, @NotNull final Throwable t) {
        warn(message,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warnSynchronized(@NotNull final String message, @NotNull final Throwable t) {
        warn(message,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warningSynchronized(@NotNull final String message, @NotNull final Throwable t) {
        warn(message,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warnAsync(@NotNull final String message, @NotNull final Throwable t) {
        executor.submit(() -> warn(message,t));
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warningAsync(@NotNull final String message, @NotNull final Throwable t) {
        executor.submit(() -> warn(message,t));
    }

    /**
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warn(@NotNull final Throwable t) {
        process(WARNING,t);
    }

    /**
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warning(@NotNull final Throwable t) {
        warn(t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warnSynchronized(@NotNull final Throwable t) {
        warn(t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warningSynchronized(@NotNull final Throwable t) {
        warn(t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warnAsync(@NotNull final Throwable t) {
        executor.submit(() -> warn(t));
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warningAsync(@NotNull final Throwable t) {
        executor.submit(() -> warn(t));
    }

    /**
     * Checks if logs at {@link Level#WARNING WARNING} level, with the specified Marker, are logged or ignored.<br>
     *     No exceptions will be thrown if the format is not followed; the arguments will simply not be added to the log.
     *     The expected format of placeholders;
     *      <pre>
     *          {@code logger.warn("Example {}.", Object);}
     *      </pre>
     * @return True | False
     */
    @Override @Contract(pure = true)
    public boolean isWarnEnabled(@NotNull final Marker marker) {
        return isLoggable(Level.WARNING, marker);
    }

    /**
     * Checks if logs at {@link Level#WARNING WARNING} level, with the specified Marker, are logged or ignored.<br>
     *     No exceptions will be thrown if the format is not followed; the arguments will simply not be added to the log.
     *     The expected format of placeholders;
     *      <pre>
     *          {@code logger.warn("Example {}.", Object);}
     *      </pre>
     * @return True | False
     */
    @Contract(pure = true)
    public boolean isWarningEnabled(@NotNull final Marker marker) {
        return isLoggable(Level.WARNING, marker);
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    @Override
    public void warn(@Nullable final Marker marker, @NotNull final String message) {
        process(WARNING,marker,message);
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warning(@Nullable final Marker marker, @NotNull final String message) {
        warn(marker,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warnSynchronized(@Nullable final Marker marker, @NotNull final String message) {
        warn(marker,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warningSynchronized(@Nullable final Marker marker, @NotNull final String message) {
        warn(marker,message);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warnAsync(@Nullable final Marker marker, @NotNull final String message) {
        executor.submit(() -> warn(marker,message));
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warningAsync(@Nullable final Marker marker, @NotNull final String message) {
        executor.submit(() -> warn(marker,message));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        process(WARNING,marker,message,arg);
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warning(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        warn(marker,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        warn(marker,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warningSynchronized(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        warn(marker,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        executor.submit(() -> warn(marker,message,arg));
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warningAsync(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        executor.submit(() -> warn(marker,message,arg));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        process(WARNING,marker,message,arg1,arg2);
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warning(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        warn(marker,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        warn(marker,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warningSynchronized(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        warn(marker,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        executor.submit(() -> warn(marker,message,arg1,arg2));
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warningAsync(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        executor.submit(() -> warn(marker,message,arg1,arg2));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        process(WARNING,marker,message,args);
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warning(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        warn(marker,message,args);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        warn(marker,message,args);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warningSynchronized(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        warn(marker,message,args);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        executor.submit(() -> warn(marker,message,args));
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warningAsync(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        executor.submit(() -> warn(marker,message,args));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        process(WARNING,marker,t,message);
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warning(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        warn(marker,message,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warnSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        warn(marker,message,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warningSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        warn(marker,message,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warnAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        executor.submit(() -> warn(marker,message,t));
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warningAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        executor.submit(() -> warn(marker,message,t));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warn(@NotNull final Marker marker, @NotNull final Throwable t) {
        process(WARNING,marker,t);
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warning(@NotNull final Marker marker, @NotNull final Throwable t) {
        warn(marker,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warnSynchronized(@NotNull final Marker marker, @NotNull final Throwable t) {
        warn(marker,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public synchronized void warningSynchronized(@NotNull final Marker marker, @NotNull final Throwable t) {
        warn(marker,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warnAsync(@NotNull final Marker marker, @NotNull final Throwable t) {
        executor.submit(() -> warn(marker,t));
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#WARNING WARNING} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isWarnEnabled() Logger#isWarnEnabled
     */
    public void warningAsync(@NotNull final Marker marker, @NotNull final Throwable t) {
        executor.submit(() -> warn(marker,t));
    }

    /**
     * Checks if logs at {@link Level#ERROR ERROR} level are logged or ignored.<br>
     *     No exceptions will be thrown if the format is not followed; the arguments will simply not be added to the log.
     *     The expected format of placeholders;
     *      <pre>
     *          {@code logger.error("Example {}.", Object);}
     *      </pre>
     * @return True | False
     */
    @Override @Contract(pure = true)
    public boolean isErrorEnabled() {
        return isLoggable(Level.ERROR);
    }

    /**
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    @Override
    public void error(@NotNull final String message) {
        process(ERROR,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public synchronized void errorSynchronized(@NotNull final String message) {
        error(message);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void errorAsync(@NotNull final String message) {
        executor.submit(() -> error(message));
    }

    /**
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    @Override
    public void error(@NotNull final String message, final Object arg) {
        process(ERROR,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public synchronized void errorSynchronized(@NotNull final String message, final Object arg) {
        error(message,arg);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void errorAsync(@NotNull final String message, final Object arg) {
        executor.submit(() -> error(message,arg));
    }

    /**
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    @Override
    public void error(@NotNull final String message, final Object arg1, final Object arg2) {
        process(ERROR,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public synchronized void errorSynchronized(@NotNull final String message, final Object arg1, final Object arg2) {
        error(message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void errorAsync(@NotNull final String message, final Object arg1, final Object arg2) {
        executor.submit(() -> error(message,arg1,arg2));
    }

    /**
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    @Override
    public void error(@NotNull final String message, Object... args) {
        process(ERROR,message,args);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public synchronized void errorSynchronized(@NotNull final String message, Object... args) {
        error(message,args);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void errorAsync(@NotNull final String message, Object... args) {
        executor.submit(() -> error(message,args));
    }

    /**
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String to log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    @Override
    public void error(@NotNull final String message, @NotNull final Throwable t) {
        process(ERROR,message,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public synchronized void errorSynchronized(@NotNull final String message, @NotNull final Throwable t) {
        error(message,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void errorAsync(@NotNull final String message, @NotNull final Throwable t) {
        executor.submit(() -> error(message,t));
    }

    /**
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void error(@NotNull final Throwable t) {
        process(ERROR,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public synchronized void errorSynchronized(@NotNull final Throwable t) {
        error(t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param t Throwable to log.
     * @apiNote Events are still triggered even if the message is not logged due to the current log level.
     * @see     Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void errorAsync(@NotNull final Throwable t) {
        executor.submit(() -> error(t));
    }

    /**
     * Checks if logs at {@link Level#ERROR ERROR} level, with the specified Marker, are logged or ignored.<br>
     * No exceptions will be thrown if the format is not followed; the arguments will simply not be added to the log.
     * The expected format of placeholders;
     *      <pre>
     *          {@code logger.error("Example {}.", Object);}
     *      </pre>
     * @return True | False
     */
    @Override @Contract(pure = true)
    public boolean isErrorEnabled(@NotNull final Marker marker) {
        return isLoggable(Level.ERROR, marker);
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    @Override
    public void error(@Nullable final Marker marker, @NotNull final String message) {
        process(ERROR,marker,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public synchronized void errorSynchronized(@Nullable final Marker marker, @NotNull final String message) {
        error(marker,message);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void errorAsync(@Nullable final Marker marker, @NotNull final String message) {
        executor.submit(() -> error(marker,message));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        process(ERROR,marker,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public synchronized void errorSynchronized(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        error(marker,message,arg);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg     Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void errorAsync(@NotNull final Marker marker, @NotNull final String message, final Object arg) {
        executor.submit(() -> error(marker,message,arg));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        process(ERROR,marker,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public synchronized void errorSynchronized(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        error(marker,message,arg1,arg2);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param arg1    Placeholder argument.
     * @param arg2    Placeholder argument.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void errorAsync(@NotNull final Marker marker, @NotNull final String message, final Object arg1, final Object arg2) {
        executor.submit(() -> error(marker,message,arg1,arg2));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        process(ERROR,marker,message,args);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public synchronized void errorSynchronized(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        error(marker,message,args);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param args    Placeholder arguments.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void errorAsync(@NotNull final Marker marker, @NotNull final String message, Object... args) {
        executor.submit(() -> error(marker,message,args));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        process(ERROR,marker,t,message);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public synchronized void errorSynchronized(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        error(marker,message,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param message The String log.
     * @param marker  The Marker to attach.
     * @param t       Throwable.
     * @apiNote       Events are still triggered even if the message is not logged due to the current log level.
     * @see           Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void errorAsync(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        executor.submit(() -> error(marker,message,t));
    }

    /**
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void error(@NotNull final Marker marker, @NotNull final Throwable t) {
        process(ERROR,marker,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public synchronized void errorSynchronized(@NotNull final Marker marker, @NotNull final Throwable t) {
        error(marker,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Logs a formatted message, with the specified Marker, at {@link Level#ERROR ERROR} level consisting of the specified message, along with all the specified arguments.
     * @param t      Throwable to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     * @see          Logger#isErrorEnabled() Logger#isErrorEnabled
     */
    public void errorAsync(@NotNull final Marker marker, @NotNull final Throwable t) {
        executor.submit(() -> error(marker,t));
    }

    /**
     * Auto-formats the exception and logs it at {@link Level#EXCEPTION EXCEPTION} level.
     * @param t      Exception to log.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     *               All logs containing exceptions will automatically be converted to an exception log.
     */
    public void exception(@NotNull final Throwable t) {
        process(EXCEPTION,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Auto-formats the exception and logs it at {@link Level#EXCEPTION EXCEPTION} level.
     * @param t      Exception to log.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     *               All logs containing exceptions will automatically be converted to an exception log.
     */
    public void exceptionSynchronized(@NotNull final Throwable t) {
        exception(t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Auto-formats the exception and logs it at {@link Level#EXCEPTION EXCEPTION} level.
     * @param t      Exception to log.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     *               All logs containing exceptions will automatically be converted to an exception log.
     */
    public void exceptionAsync(@NotNull final Throwable t) {
        executor.submit(() -> exception(t));
    }

    /**
     * Auto-formats the exception and logs it, with the specified Marker, at {@link Level#EXCEPTION EXCEPTION} level.
     * @param t      Exception to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     *               All logs containing exceptions will automatically be converted to an exception log.
     */
    public void exception(@NotNull final Marker marker, @NotNull final Throwable t) {
        process(EXCEPTION,marker,t);
    }

    /**
     * <strong>This operation is <i>synchronous.</i></strong><hr>
     * Auto-formats the exception and logs it, with the specified Marker, at {@link Level#EXCEPTION EXCEPTION} level.
     * @param t      Exception to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     *               All logs containing exceptions will automatically be converted to an exception log.
     */
    public void exceptionSynchronized(@NotNull final Marker marker, @NotNull final Throwable t) {
        exception(marker,t);
    }

    /**
     * <strong>This operation is <i>asynchronous.</i></strong><hr>
     * Auto-formats the exception and logs it, with the specified Marker, at {@link Level#EXCEPTION EXCEPTION} level.
     * @param t      Exception to log.
     * @param marker The Marker to attach.
     * @apiNote      Events are still triggered even if the message is not logged due to the current log level.
     *               All logs containing exceptions will automatically be converted to an exception log.
     */
    public void exceptionAsync(@NotNull final Marker marker, @NotNull final Throwable t) {
        executor.submit(() -> exception(marker,t));
    }
}