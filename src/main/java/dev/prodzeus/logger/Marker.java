package dev.prodzeus.logger;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A simple SLF4J Marker implementation that does <b>not</b> allow references to other Markers.
 */
public class Marker implements org.slf4j.Marker {

    private final String name;

    @Contract(pure = true)
    public Marker(@NotNull final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void add(@Nullable final org.slf4j.Marker reference) {
        //Ignored.
    }

    @Override
    public boolean remove(@Nullable final org.slf4j.Marker reference) {
        //Ignored.
        return true;
    }

    @Override
    public boolean hasChildren() {
        //Ignored.
        return false;
    }

    @Override
    public boolean hasReferences() {
        //Ignored.
        return false;
    }

    @Override
    public @NotNull Iterator<org.slf4j.Marker> iterator() throws NoSuchElementException {
        //Ignored.
        final Marker marker = this;
        return new Iterator<>() {
            int i = 1;

            @Override
            public boolean hasNext() {
                return i > 0;
            }

            @Override
            public org.slf4j.Marker next() {
                if (i-- > 0) return marker;
                else throw new NoSuchElementException();
            }
        };
    }

    @Override
    public boolean contains(@NotNull final org.slf4j.Marker other) {
        return false;
    }

    @Override
    public boolean contains(@NotNull final String name) {
        return false;
    }

    @Contract("_ -> new")
    public static @NotNull Marker of(@NotNull final org.slf4j.Marker marker) {
        return new Marker(marker.getName());
    }
}
