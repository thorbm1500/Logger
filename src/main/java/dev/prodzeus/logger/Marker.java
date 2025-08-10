package dev.prodzeus.logger;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Markers are named objects used to enrich log statements.
 * The Markers in this implementation of SLF4J can be used in 2 different ways.
 * <ol>
 *     <li>Add extra tags to logs for better readability.</li>
 *     <li>Ensure certain logs are always logged, by registering the Markers as <i>Forced Markers</i>.</li>
 * </ol>
 * <p>
 *      Some implementations value the Markers' data; however, the data of a Marker is ignored entirely in this implementation.
 *      This implementation instead relies on the practice of registering <i>Forced Markers</i>, and otherwise only
 *      using Markers for readability and customizability of logs.
 * </p>
 * @see Logger#registerForcedMarker(org.slf4j.Marker) Logger#registerForcedMarker
 */
public final class Marker implements org.slf4j.Marker {

    private final String name;
    private final Set<org.slf4j.Marker> references = Collections.synchronizedSet(new HashSet<>());

    @Contract(pure = true)
    Marker(@NotNull final String name) {
        this.name = name;
    }

    /**
     * Gets the name of the Marker.
     * @return The name of the Marker.
     */
    @Override @Contract(pure = true)
    public @NotNull String getName() {
        return name;
    }

    /**
     * Adds a reference to the specified Marker.
     * <p>
     *     More specifically, the specified Marker will become a referenced marker for <i>this Marker</i>
     *     and the referenced Marker will in of itself not be aware of this,
     *     nor have any connection or relation to <i>this Marker</i>, after calling this method.
     *     Only <i>this Marker</i> is aware of the reference.
     * </p>
     *
     * @param   reference Marker to add reference to.
     * @apiNote Markers can contain references to nested markers, which in turn may
     *          contain references of their own.
     *          Note that the fluent API (new in 2.0) allows adding
     *          multiple markers to a logging statement.
     *          It is <i>often preferable</i> and recommended to use
     *          multiple markers instead of nested markers.
     */
    @Override
    public void add(@NotNull final org.slf4j.Marker reference) {
        references.add(reference);
    }

     /**
     * Remove the reference from this Marker to the specified reference Marker.
     * @param reference The Marker to remove.
     * @return True, if the Marker was removed, otherwise false.
     * False simply might also indicate that the Marker was never a referenced Marker to begin with.
     * @see Marker#removeIfPresent(org.slf4j.Marker)
     */
    @Override
    public boolean remove(@NotNull final org.slf4j.Marker reference) {
        return references.remove(reference);
    }

    /**
     * Remove the reference from this Marker to the specified reference Marker.
     * <p>
     * This method is essentially the same as {@link Marker#remove},
     * but instead only returns True if the Marker was present,
     * and has been successfully removed from references.
     * </p>
     * @param reference The Marker to remove.
     * @return True, if the Marker was present and has been removed, otherwise false.
     */
    public boolean removeIfPresent(@NotNull final org.slf4j.Marker reference) {
        return reference.contains(reference) && references.remove(reference);
    }

    /**
     * @deprecated Replaced by {@link #hasReferences()}.
     */
    @Override @Deprecated @Contract(pure = true)
    public boolean hasChildren() {
        return !references.isEmpty();
    }

    /**
     * Checks if this instance contains any references to other Markers.
     * @return True, if any references are present, otherwise false.
     * @see Marker#contains(org.slf4j.Marker)
     */
    @Override @Contract(pure = true)
    public boolean hasReferences() {
        return !references.isEmpty();
    }

    /**
     * Get an Iterator of all Markers associated with this marker.
     * The first call to {@link Iterator#next()} will <i>always</i> return this instance.
     * @return A new Iterator.
     * @throws NoSuchElementException If {@link Iterator#next()} is called when no more elements are present.
     */
    @Override @Contract(pure = true)
    public @NotNull Iterator<org.slf4j.Marker> iterator() throws NoSuchElementException {
        final Deque<org.slf4j.Marker> collection = new ArrayDeque<>(references);
        collection.offerFirst(this);

        return new Iterator<>() {
            final Deque<org.slf4j.Marker> markers = collection;

            @Override
            public boolean hasNext() {
                return !collection.isEmpty();
            }

            @Override
            public org.slf4j.Marker next() {
                if (hasNext()) return markers.poll();
                else throw new NoSuchElementException();
            }
        };
    }

    /**
     * Check if this instance contains a reference to the specified Marker.
     * @param other The Marker to check for reference to.
     * @return True, if a reference is present, otherwise false.
     */
    @Override @Contract(pure = true)
    public boolean contains(@NotNull final org.slf4j.Marker other) {
        return references.contains(other);
    }

    /**
     * Check if this instance contains a reference to a Marker with the specified name.
     * @param name The name of the Marker to check for reference to.
     * @return True, if a reference is present, otherwise false.
     */
    @Override @Contract(pure = true)
    public boolean contains(@NotNull final String name) {
         return references.stream().anyMatch(marker -> marker.getName().equals(name));
    }

    @Override @Contract(pure = true)
    public int hashCode() {
        return name.hashCode();
    }
}
