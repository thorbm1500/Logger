package dev.prodzeus.logger;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.IMarkerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The Marker Factory is responsible for handling existing Markers and creating new Markers.
 * @apiNote Factory operations are Thread safe.
 */
public final class MarkerFactory implements IMarkerFactory {

    private final ConcurrentHashMap<String, Marker> markers = new ConcurrentHashMap<>(16);

    /**
     * Gets the Marker with the given name.
     * If no Markers are present with the given name,
     * a new Marker instance will be created and returned.
     * @param name The name of the Marker.
     * @return The existing or new Marker.
     */
    @Override @Contract(pure = true)
    public @NotNull Marker getMarker(@NotNull final String name) {
        return markers.computeIfAbsent(name, Marker::new);
    }

    /**
     * Checks if any Markers exist with the specified name.
     * @param name The name of the Marker.
     * @return True, if a Marker with the specified name is present, otherwise false.
     */
    @Override @Contract(pure = true)
    public boolean exists(@NotNull final String name) {
        return markers.containsKey(name);
    }

    /**
     * Removes a Marker from the Collection of existing Markers and returns it.
     * <p>
     *     This makes the Marker detached from the Factory, and will no
     *     longer be managed by the Factory.
     * </p>
     *
     * @param name The name of the Marker.
     * @return The Marker if any Marker with the specified name exists, otherwise null.
     * @see MarkerFactory#exists(String)
     */
    public @Nullable Marker remove(@NotNull final String name) {
        return markers.remove(name);
    }

    /**
     * Detaches a Marker of the Factory.
     * <p>
     *      Detaching a Marker makes it independent of the Factory,
     *      and no longer managed by the Factory.
     *      This is essentially the same as removing the Marker instance,
     *      as it's simply being removed from the Collection of existing Markers.
     * </p>
     *
     * @param name The name of the Marker to detach.
     * @return     True, if the specified Marker was present and detached, otherwise false.
     * @see        MarkerFactory#detachAndGetMarker(String)
     * @apiNote    A detached Marker will not be aware of <i>any</i> changes to existing Markers,
     *             even if changes are to happen to the Marker's referenced Markers.
     */
    @Override
    public boolean detachMarker(@NotNull final String name) {
        return markers.remove(name) != null;
    }

    /**
     * Detaches the Marker with the specified name, and returns it.
     * Calling this method will provide the same result as calling {@link MarkerFactory#remove(String)}.
     *
     * @param name The name of the Marker to detach.
     * @return     The Marker if any Marker with the specified name exists, otherwise null.
     * @apiNote    A detached Marker will not be aware of <i>any</i> changes to existing Markers,
     *             even if changes are to happen to the Marker's referenced Markers.
     */
    @Contract(pure = true)
    public @Nullable Marker detachAndGetMarker(@NotNull final String name) {
        return markers.remove(name);
    }

    /**
     * Creates and returns a new detached Marker.
     * <p>
     *     This marker is detached for the <i>entirety</i> of its existence,
     *     and will <i>never</i> have any relation to the Factory.
     *     Detached Markers are useful for situations where a
     *     temporary Marker is needed.
     * </p>
     * @param name The name of the Marker.
     * @return     The new detached Marker.
     */
    @Override @Contract(pure = true)
    public @NotNull Marker getDetachedMarker(@NotNull final String name) {
        return new Marker(name);
    }
}
