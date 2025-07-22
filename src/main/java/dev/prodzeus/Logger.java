package dev.prodzeus;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.regex.Matcher;

public class Logger {

    private static String logPrefix = "";
    public static Level level = Level.INFO;
    private static Consumer<String> logConsumer = null;

    private Logger(final String logger) {
        logPrefix = logger.stripTrailing().concat(" ");
    }

    /**
     * Set the prefix for the logger.<br>
     * Format printed:
     * <code>prefix [LOGLEVEL] Log text goes here.</code>
     * @param prefix The new log prefix.
     * @apiNote A blank prefix can be provided to disable it all-together.
     * @return The Logger instance.
     */
    public Logger setPrefix(final String prefix) {
        logPrefix = prefix.stripTrailing().concat(" ");
        return this;
    }

    /**
     * Add a consumer to be called on each LogEvent
     * @param consumer Consumer.
     * @return The Logger instance.
     */
    public Logger onLog(@NotNull final Consumer<String> consumer) {
        logConsumer = consumer;
        return this;
    }

    private void logEvent(@NotNull final Level level, @NotNull final String log) {
        if (logConsumer != null) logConsumer.accept(log);
    }

    private void print(@NotNull final Level level, @NotNull final String log) {
        System.out.println(logPrefix + level.getPrefix() + " " + log);
        logEvent(level,log);
    }

    private void print(@NotNull final Level level, @NotNull final String log, @NotNull final Object... args) {
        print(level,format(log,args));
    }

    public void log(@NotNull final Level level, @NotNull final String log) {
        print(level, log);
    }

    public void log(@NotNull final Level level, @NotNull final String log, @NotNull final Object... args) {
        print(level,log,args);
    }

    public void debug(@NotNull final String log) {
        print(Level.DEBUG,log);
    }

    public void debug(@NotNull final String log, @NotNull final Object... args) {
        print(Level.DEBUG,log,args);
    }

    public void info(@NotNull final String log) {
        print(Level.INFO,log);
    }

    public void info(@NotNull final String log, @NotNull final Object... args) {
        print(Level.INFO,log,args);
    }

    public void warn(@NotNull final String log) {
        print(Level.WARNING,log);
    }

    public void warn(@NotNull final String log, @NotNull final Object... args) {
        print(Level.WARNING,log,args);
    }

    public void error(@NotNull final String log) {
        print(Level.ERROR,log);
    }

    public void error(@NotNull final String log, @NotNull final Object... args) {
        print(Level.ERROR,log,args);
    }

    public void severe(@NotNull final String log) {
        print(Level.SEVERE,log);
    }

    public void severe(@NotNull final String log, @NotNull final Object... args) {
        print(Level.SEVERE,log,args);
    }

    public void fatal(@NotNull final String log) {
        print(Level.SEVERE,log);
    }

    public void fatal(@NotNull final String log, @NotNull final Object... args) {
        print(Level.SEVERE,log,args);
    }

    private static String format(@NotNull String log, @NotNull final Object... args) {
        for (final Object arg : args) {
            switch (arg) {
                case String s -> log = log.replaceFirst("\\{}", Matcher.quoteReplacement(s));
                case Exception e -> {
                    String replacement = "";
                    for (final StackTraceElement st : e.getStackTrace()) {
                        replacement = replacement.concat(st.toString()).concat("\n");
                    }
                    log = log.replaceFirst("\\{}", Matcher.quoteReplacement(replacement));
                }
                case Throwable t -> {
                    String replacement = "";
                    for (final StackTraceElement st : t.getStackTrace()) {
                        replacement = replacement.concat(st.toString()).concat("\n");
                    }
                    log = log.replaceFirst("\\{}", Matcher.quoteReplacement(replacement));
                }
                default -> log = log.replaceFirst("\\{}", Matcher.quoteReplacement(String.valueOf(arg)));
            }
        }
        return log;
    }

    public static Logger getLogger(final String logger) {
        return new Logger(logger);
    }
}