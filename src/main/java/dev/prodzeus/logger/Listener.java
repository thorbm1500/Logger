package dev.prodzeus.logger;

import dev.prodzeus.logger.components.Level;
import dev.prodzeus.logger.internal.ListenerRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * <p>
 *     All logs will individually trigger their corresponding Event when called.
 *     A log at the level {@link Level#INFO INFO} will fx, trigger the event {@link Listener#onInfoLogEvent(Event) Listener#onInfoLogEvent}.
 *     All information relevant to the log will be contained inside the {@link Event}, making it
 *     available for manual manipulation such as; saving logs to a file, forwarding logs to other services, etc.
 * </p>
 * <p>
 *     Logs will <i>always</i> trigger their corresponding {@link Event}, regardless of the level they're logged at,
 *     and both the Global and individual Logger level.
 *     This logic is used to ensure complete manual control over all logs.
 * </p>
 *     The recommended way of creating and registering a new Listener;
 *     <pre>
 *         {@code final Listener listener = new EventListener(logger).register();}
 *     </pre>
 * <p>
 *      <i>All logs</i>, regardless of its type, will trigger the {@link Listener#onGenericLogEvent(Event) Listener#onGenericLogEvent}.
 *      Listening to the Generic Event is useful if you with to manipulate multiple log levels the same way,
 *      instead of creating a method for each level.
 * </p>
 * <p>
 *      Systems implementing SLF4J will <i>also</i> trigger Events when logging.
 *      You're responsible for filtering the logs yourself if this is not wanted.
 * </p>
 * <hr><b>Note: All Events are fired <i>asynchronous</i>.</b>
 * @see SLF4JProvider#getGlobalLevel() SLF4JProvider#getGlobalLevel
 */
@SuppressWarnings("EmptyMethod")
public abstract class Listener {

    private final UUID uuid = UUID.randomUUID();
    private final Logger owner;

    /**
     * Removes and unregisters the Listener from the Listener registry.
     * @apiNote This instance will be <b><i>unusable</i></b> after calling this method.
     */
    public final void destroy() {
        ListenerRegistry.unregister(this);
    }

    /**
     * Registers the Listener in the Listener registry.
     * @see Listener#destroy() Listener#destroy
     * @throws IllegalArgumentException If no methods are overridden, as the Listener will then be treated as being empty.
     * @return The Listener instance.
     */
    @Contract(pure = true)
    public final @NotNull Listener register() {
        ListenerRegistry.register(this);
        return this;
    }

    /**
     * Creates a new instance of a Listener.
     * @param logger The Logger instance that's considered the Owner of the Listener.
     * @see Listener#register() Listener#register
     */
    @Contract(pure = true)
    protected Listener(@NotNull final Logger logger) {
        this.owner = logger;
    }

    /**
     * Gets the UUID of the Listener. Each Listener is assigned a random UUID as their unique identifier.
     * @return The Listener's UUID.
     */
    @Contract(pure = true)
    public final @NotNull UUID getUniqueId() {
        return uuid;
    }

    /**
     * Gets the Logger considered to be the Owner of the Listener instance.
     * @return The Logger.
     */
    @Contract(pure = true)
    public final @NotNull Logger getOwner() {
        return owner;
    }

    /**
     * This Event is triggered on <i>all</i> logs.
     * @param event The event triggered.
     */
    public void onGenericLogEvent(@NotNull final Event event) {}

    /**
     * This Event is triggered on all {@link Level#TRACE TRACE} logs.
     * @param event The event triggered.
     */
    public void onTraceLogEvent(@NotNull final Event event) {}

    /**
     * This Event is triggered on all {@link Level#DEBUG DEBUG} logs.
     * @param event The event triggered.
     */
    public void onDebugLogEvent(@NotNull final Event event) {}

    /**
     * This Event is triggered on all {@link Level#INFO INFO} logs.
     * @param event The event triggered.
     */
    public void onInfoLogEvent(@NotNull final Event event) {}

    /**
     * This Event is triggered on all {@link Level#WARNING WARNING} logs.
     * @param event The event triggered.
     */
    public void onWarnLogEvent(@NotNull final Event event) {}

    /**
     * This Event is triggered on all {@link Level#ERROR ERROR} logs.
     * @param event The event triggered.
     */
    public void onErrorLogEvent(@NotNull final Event event) {}

    /**
     * This Event is triggered on all {@link Level#EXCEPTION EXCEPTION} logs.
     * @param event The event triggered.
     */
    public void onExceptionEvent(@NotNull final Event event) {}

    @Override @Contract(pure = true)
    public final boolean equals(Object o) {
        return o instanceof Listener l && l.uuid.equals(this.uuid);
    }

    @Override @Contract(pure = true)
    public final int hashCode() {
        return Objects.hash(uuid,owner);
    }
}
