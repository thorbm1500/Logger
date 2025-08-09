package dev.prodzeus.logger;

import dev.prodzeus.logger.components.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;

import java.time.Instant;
import java.util.Collection;

/**
 * An Event is a "container" type Object created everytime a new log is logged.
 * Each Event contains all the relevant information of the log that triggered it,
 * and provides a way of retrieving and manipulating the data.
 * @apiNote By the time an Event is accessible, it has <i>already been fired</i>,
 * and no changes to the Event will be reflected in the original log.
 */
public final class Event {

    private final Logger logger;
    private final Level level;
    private final Marker marker;
    private final Throwable exception;
    private final String message;
    private final String rawMessage;
    private final Collection<Object> args;
    private final Instant timestamp = Instant.now();

    @Contract(pure = true)
    Event(@NotNull final Logger logger, @NotNull final Level level, @Nullable final Marker marker, @Nullable final Throwable throwable, @NotNull final String rawMessage, @NotNull final String message, @NotNull final Collection<Object> args) {
        this.logger = logger;
        this.level = level;
        this.marker = marker;
        this.rawMessage = rawMessage;
        this.message = message;
        this.args = args;
        this.exception = throwable;
    }

    /**
     * Gets the Logger that triggered the Event.
     * @return The Logger.
     */
    @Contract(pure = true)
    public @NotNull Logger getLogger() {
        return logger;
    }

    /**
     * Gets the log level of the original log that triggered this Event.
     * @return The log level.
     * @apiNote The level returned might differ from the log's original level.
     *          Passed arguments such as {@link Throwable}s and {@link net.dv8tion.jda.api.requests.ErrorResponse}s can alter the level before it's logged and the Event is fired.
     */
    @Contract(pure = true)
    public @NotNull Level getLevel() {
        return level;
    }

    /**
     * Gets the Marker attached to the log message.
     * @return The Marker attached, if any, otherwise null.
     */
    @Contract(pure = true)
    public @Nullable Marker getMarker() {
        return marker;
    }

    /**
     * Gets the message logged.
     * @return The message logged.
     * @see Event#getRawMessage()Event#getRawMessage
     * @apiNote This message is formatted to the ASCII format, for coloring the logs in terminals.
     *          Events also contain the messages in their "raw" form, with all formatting stripped.
     */
    @Contract(pure = true)
    public @NotNull String getMessage() {
        return message;
    }

    /**
     * Gets the raw message without any text formatting.
     * <p>
     *      This version of the message has all formatting stripped from it,
     *      and is therefore a <i>"safe"</i> version of the log message,
     *      to use for extra logging elsewhere.
     * </p>
     * @return The raw log message.
     * @apiNote This version will still include all prefixes, such as the Logger's name, the log level prefix, etc.
     */
    @Contract(pure = true)
    public @NotNull String getRawMessage() {
        return rawMessage;
    }

    /**
     * Checks if any arguments were passed to the log.
     * @return True, if one or more arguments are present, otherwise false.
     */
    @Contract(pure = true)
    public boolean hasArguments() {
        return !args.isEmpty();
    }

    /**
     * Gets the collection of arguments passed to the log.
     * @return An immutable collection of the log's arguments.
     * @see Event#hasArguments()Event#hasArguments
     */
    @Contract(pure = true)
    public @NotNull Collection<Object> getArguments() {
        return args;
    }

    /**
     * Checks if an Exception was logged.
     * @return  True, if an Exception is present, otherwise false.
     */
    @Contract(pure = true)
    public boolean hasException() {
        return exception != null;
    }

    /**
     * Gets the Exception stored in the Event. <i>The Event might not contain an Exception.</i>
     * <p>
     *      When Exceptions are logged, they're added to the Events after logging,
     *      regardless of if they were logged by themselves or passed as an argument, to allow for further manual manipulation.
     * </p>
     * @return The Exception if one is present, otherwise null.
     * @apiNote Exceptions will <i>always</i> be present in the collection of arguments,
     *          regardless of how they were logged.
     * @see Event#hasException()Event#hasException
     */
    @Contract(pure = true)
    public @Nullable Throwable getException() {
        return exception;
    }

    /**
     * Gets the timestamp of the Event as an {@link Instant}.
     * @return The Event's timestamp.
     * @apiNote The timestamp represents the time of when the Event was created, and <i>not</i> when it was fired.
     */
    @Contract(pure = true)
    public @NotNull Instant getTimeCreated() {
        return timestamp;
    }
}