package dev.prodzeus.logger;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 * A simple logger with SLF4J implementation.
 * @apiNote SLF4J Version: <b>2.0.12</b>
 * @author prodzeus
 */
@SuppressWarnings("unused")
public class Logger implements org.slf4j.Logger {

    private final String name;
    private Level level = Level.INFO;
    private final Set<Marker> forcedMarkers = new HashSet<>();
    private final Set<Consumer<String>> consumer = new HashSet<>();
    private boolean alwaysRunConsumers = false;

    /**
     * Constructs a new Logger instance.
     * @param name Name of the new Logger instance.
     * @apiNote <b>This should only be called through the LoggerFactory!</b>
     * @see LoggerFactory#getLogger(String)
     */
    public Logger(final String name) {
        this.name = name;
        log(Level.INFO,"\u001b[38;5;46mNew Logger instance created.");
    }

    /**
     * Register a forced {@link Marker}.
     * @param marker Marker to register.
     * @apiNote A log with a forced marker will <b>always</b> be logged, regardless of the current {@link Level}.
     * @return The Logger instance.
     */
    public Logger registerForcedMarker(@NotNull final Marker marker) {
        this.forcedMarkers.add(marker);
        return this;
    }

    /**
     * Unregister a forced {@link Marker}.
     * @param marker Marker to unregister.
     * @return The Logger instance.
     */
    public Logger unregisterForcedMarker(@NotNull final Marker marker) {
        this.forcedMarkers.remove(marker);
        return this;
    }

    /**
     * Clear all registered forced {@link Marker}s.
     * @return The Logger instance.
     */
    public Logger clearForcedMarkers() {
        this.forcedMarkers.clear();
        return this;
    }

    /**
     * Set the current Log Level. Any log call below this level will be ignored.
     * @param level New Log Level.
     * @return The Logger instance.
     */
    public Logger setLevel(@NotNull final Level level) {
        this.level = level;
        return this;
    }

    /**
     * Get the current log level set.
     * Any logs logged below this level will be ignored,
     * unless a forced marker has been registered.
     * @return The current Level.
     * @see Logger#registerForcedMarker(Marker)
     */
    @NotNull
    @Contract(pure = true)
    public Level getLevel() {
        return level;
    }

    /**
     * Check if a level is loggable.
     * @param level The Level to check.
     * @return True, if log calls at this level are logged to console, otherwise false.
     */
    @Contract(pure = true)
    public boolean isLoggable(@NotNull final Level level) {
        return this.level.getWeight() <= level.getWeight();
    }

    /**
     * Check if a level or marker is loggable.
     * @param marker The marker.
     * @param level The level.
     * @return True, if the level is equal to or higher than the current Log Level,
     * or if the marker is a registered forced marker, otherwise false.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Contract(pure = true)
    public boolean isLoggable(@NotNull final Marker marker, final Level level) {
        if (forcedMarkers.contains(marker)) return true;
        return isLoggable(level);
    }

    private void log(@NotNull final String message) {
        System.out.println(message);
    }

    private void log(@NotNull final Level level, @NotNull final String message) {
        if (isLoggable(level)) {
            log("%s \u001b[38;5;240m[\u001b[0m%s\u001b[38;5;240m]\u001b[0m: %s %s".formatted(level.getPrefix(), name, level.getColor(), message));
            consumer.forEach(c -> c.accept("%s [%s]: %s".formatted(level.name(), name, message)));
        }
        else if (alwaysRunConsumers) consumer.forEach(c -> c.accept("%s [%s]: %s".formatted(level.name(), name, message)));
    }

    private void log(@NotNull final Level level, @NotNull final Marker marker, @NotNull final String message) {
        if (isLoggable(marker,level)) {
            log("%s \u001b[38;5;240m[\u001b[0m%s\u001b[38;5;240m] [\u001b[0m%s\u001b[38;5;240m]: %s %s".formatted(level.getPrefix(), marker.getName(), name, level.getColor(), message));
            consumer.forEach(c -> c.accept("%s [%s] [%s]: %s".formatted(level.name(), marker.getName(), name, message)));
        }
        else if (alwaysRunConsumers) consumer.forEach(c -> c.accept("%s [%s] [%s]: %s".formatted(level.name(), marker.getName(), name, message)));
    }

    /**
     * Whether consumers should ignore the Log Level.
     * Setting this to true will ensure that consumers are always passed the log,
     * regardless of the current Log Level.
     * @param enable True | False
     * @return The Logger instance.
     */
    public Logger alwaysRunConsumers(final boolean enable) {
        this.alwaysRunConsumers = enable;
        return this;
    }

    /**
     * Add a consumer to the Logger.
     * Any log call will be passed to all registered consumers.
     * @param consumer New consumer to add.
     * @return The Logger instance.
     *
     */
    public Logger registerConsumer(@NotNull final Consumer<String> consumer) {
        this.consumer.add(consumer);
        return this;
    }

    /**
     * Unregister a consumer from the Logger.
     * @param consumer Consumer to unregister.
     * @return The Logger instance.
     */
    public Logger unregisterConsumer(@NotNull final Consumer<String> consumer) {
        this.consumer.remove(consumer);
        return this;
    }

    /**
     * Clear all registered Consumers.
     * @return The Logger instance.
     */
    public Logger clearConsumers() {
        this.consumer.clear();
        return this;
    }

    /**
     * Get the name of the Logger instance.
     * @return The name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Check if log calls to Log Level {@link Level#TRACE} will be logged or ignored.
     * @return True | False
     */
    @Override
    public boolean isTraceEnabled() {
        return isLoggable(Level.TRACE);
    }

    @Override
    public void trace(@NotNull final String message) {
        log(Level.TRACE,message);
    }

    @Override
    public void trace(@NotNull String message, @NotNull final Object arg) {
        trace(format(message,arg));
    }

    @Override
    public void trace(@NotNull String message, @NotNull final Object arg1, final Object arg2) {
        trace(format(message,arg1,arg2));
    }

    @Override
    public void trace(@NotNull String message, @NotNull final Object... args) {
        trace(format(message,args));
    }

    @Override
    public void trace(@NotNull String message, @NotNull final Throwable t) {
        trace(format(message,t));
    }

    /**
     * Check if log calls to Log Level {@link Level#TRACE} with the given {@link Marker} will be logged or ignored.
     * @return True, if the current Log Level is of Level Trace, or if the Marker is a registered forced marker.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Override
    public boolean isTraceEnabled(@NotNull final Marker marker) {
        return isLoggable(marker,Level.TRACE);
    }

    @Override
    public void trace(@NotNull final Marker marker, @NotNull final String message) {
        log(Level.TRACE,marker,message);
    }

    @Override
    public void trace(@NotNull final Marker marker, String message, @NotNull final Object arg) {
        trace(marker,format(message,arg));
    }

    @Override
    public void trace(@NotNull final Marker marker, String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        trace(marker,format(message,arg1,arg2));
    }

    @Override
    public void trace(@NotNull final Marker marker, String message, Object... args) {
        trace(marker,format(message,args));
    }

    @Override
    public void trace(@NotNull final Marker marker, String message, @NotNull final Throwable t) {
        trace(marker,format(message,t));
    }

    /**
     * Check if log calls to Log Level {@link Level#DEBUG} will be logged or ignored.
     * @return True | False
     */
    @Override
    public boolean isDebugEnabled() {
        return isLoggable(Level.DEBUG);
    }

    @Override
    public void debug(@NotNull final String message) {
        log(Level.DEBUG,message);
    }

    @Override
    public void debug(@NotNull final String message, @NotNull final Object arg) {
        debug(format(message,arg));
    }

    @Override
    public void debug(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        debug(format(message,arg1,arg2));
    }

    @Override
    public void debug(@NotNull final String message, @NotNull final Object... args) {
        debug(format(message,args));
    }

    @Override
    public void debug(@NotNull final String message, @NotNull final Throwable t) {
        debug(format(message,t));
    }

    /**
     * Check if log calls to Log Level {@link Level#DEBUG} with the given {@link Marker} will be logged or ignored.
     * @return True, if the current Log Level is of Level Debug, or if the Marker is a registered forced marker.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Override
    public boolean isDebugEnabled(@NotNull final Marker marker) {
        return isLoggable(marker,Level.DEBUG);
    }

    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message) {
        log(Level.DEBUG,marker,message);
    }

    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        debug(marker,format(message,arg));
    }

    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        debug(marker,format(message,arg1,arg2));
    }

    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... args) {
        debug(marker,format(message,args));
    }

    @Override
    public void debug(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        debug(marker,format(message,t));
    }

    /**
     * Check if log calls to Log Level {@link Level#INFO} will be logged or ignored.
     * @return True | False
     */
    @Override
    public boolean isInfoEnabled() {
        return isLoggable(Level.INFO);
    }

    @Override
    public void info(@NotNull final String message) {
        log(Level.INFO,message);
    }

    @Override
    public void info(@NotNull final String message, @NotNull final Object arg) {
        info(format(message,arg));
    }

    @Override
    public void info(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        info(format(message,arg1,arg2));
    }

    @Override
    public void info(@NotNull final String message, @NotNull final Object... arguments) {
        info(format(message,arguments));
    }

    @Override
    public void info(@NotNull final String message, @NotNull final Throwable t) {
        info(format(message,t));
    }

    /**
     * Check if log calls to Log Level {@link Level#INFO} with the given {@link Marker} will be logged or ignored.
     * @return True, if the current Log Level is of Level Info, or if the Marker is a registered forced marker.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Override
    public boolean isInfoEnabled(@NotNull final Marker marker) {
        return isLoggable(marker,Level.INFO);
    }

    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message) {
        log(Level.INFO,marker,message);
    }

    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        info(marker,format(message,arg));
    }

    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        info(marker,format(message,arg1,arg2));
    }

    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... arguments) {
        info(marker,format(message,arguments));
    }

    @Override
    public void info(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        info(marker,format(message,t));
    }

    /**
     * Check if log calls to Log Level {@link Level#WARNING} will be logged or ignored.
     * @return True | False
     */
    @Override
    public boolean isWarnEnabled() {
        return isLoggable(Level.WARNING);
    }

    @Override
    public void warn(@NotNull final String message) {
        log(Level.WARNING,message);
    }

    @Override
    public void warn(@NotNull final String message, @NotNull final Object arg) {
        warn(format(message,arg));
    }

    @Override
    public void warn(@NotNull final String message, @NotNull final Object... arguments) {
        warn(format(message,arguments));
    }

    @Override
    public void warn(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        warn(format(message,arg1,arg2));
    }

    @Override
    public void warn(@NotNull final String message, @NotNull final Throwable t) {
        warn(format(message,t));
    }

    /**
     * Check if log calls to Log Level {@link Level#WARNING} with the given {@link Marker} will be logged or ignored.
     * @return True, if the current Log Level is of Level Warning, or if the Marker is a registered forced marker.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Override
    public boolean isWarnEnabled(@NotNull final Marker marker) {
        return isLoggable(marker,Level.WARNING);
    }

    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message) {
        log(Level.WARNING,marker,message);
    }

    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        warn(marker,format(message,arg));
    }

    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        warn(marker,format(message,arg1,arg2));
    }

    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... arguments) {
        warn(marker,format(message,arguments));
    }

    @Override
    public void warn(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        warn(marker,format(message,t));
    }

    /**
     * Check if log calls to Log Level {@link Level#ERROR} will be logged or ignored.
     * @return True | False
     */
    @Override
    public boolean isErrorEnabled() {
        return isLoggable(Level.ERROR);
    }

    @Override
    public void error(@NotNull final String message) {
        log(Level.ERROR,message);
    }

    @Override
    public void error(@NotNull final String message, @NotNull final Object arg) {
        error(format(message,arg));
    }

    @Override
    public void error(@NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        error(format(message,arg1,arg2));
    }

    @Override
    public void error(@NotNull final String message, @NotNull final Object... arguments) {
        error(format(message,arguments));
    }

    @Override
    public void error(@NotNull final String message, @NotNull final Throwable t) {
        error(format(message,t));
    }

    /**
     * Check if log calls to Log Level {@link Level#ERROR} with the given {@link Marker} will be logged or ignored.
     * @return True, if the current Log Level is of Level Error, or if the Marker is a registered forced marker.
     * @see Logger#registerForcedMarker(Marker)
     */
    @Override
    public boolean isErrorEnabled(@NotNull final Marker marker) {
        return isLoggable(marker,Level.ERROR);
    }

    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message) {
        log(Level.ERROR,marker,message);
    }

    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg) {
        error(marker,format(message,arg));
    }

    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object arg1, @NotNull final Object arg2) {
        error(marker,format(message,arg1,arg2));
    }

    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, @NotNull final Object... arguments) {
        error(marker,format(message,arguments));
    }

    @Override
    public void error(@NotNull final Marker marker, @NotNull final String message, @NotNull final Throwable t) {
        error(marker,format(message,t));
    }

    @NotNull
    private static String format(@NotNull final String log, @NotNull final Object... args) {
        String message = log;
        for (final Object arg : args) {
            switch (arg) {
                case Throwable t -> {
                    final StringBuilder builder = new StringBuilder();
                    for (final StackTraceElement st : t.getStackTrace()) {
                        builder.append(st.toString()).append("\n");
                    }
                    message = message.replaceFirst("\\{}", Matcher.quoteReplacement(builder.toString()));
                }
                case Collection<?> c -> {
                    final StringBuilder builder = new StringBuilder();
                    for (final Object o : c) {
                        builder.append(String.valueOf(o)).append("\n");
                    }
                    message = message.replaceFirst("\\{}", Matcher.quoteReplacement(builder.toString()));
                }
                case Map<?,?> m -> {
                    final StringBuilder builder = new StringBuilder();
                    builder.append("{");
                    for (final var index : m.entrySet()) {
                        builder.append(" [ %s , %s ] ".formatted(String.valueOf(index.getKey()), String.valueOf(index.getValue())));
                    }
                    builder.append("}");
                    message = message.replaceFirst("\\{}", Matcher.quoteReplacement(builder.toString()));
                }
                case String s -> message = message.replaceFirst("\\{}", Matcher.quoteReplacement(s));
                default -> message = message.replaceFirst("\\{}", Matcher.quoteReplacement(arg.toString()));
            }
        }
        return message;
    }
}