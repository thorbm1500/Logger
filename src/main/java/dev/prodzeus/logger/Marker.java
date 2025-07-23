package dev.prodzeus.logger;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * A simple SLF4J Marker implementation that does <b>not</b> allow references to other Markers.
 */
public class Marker implements org.slf4j.Marker {

    private final String name;

    public Marker(@NotNull final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void add(org.slf4j.Marker reference) {
        //does nothing.
    }

    @Override
    public boolean remove(org.slf4j.Marker reference) {
        //does nothing.
        return true;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean hasReferences() {
        return false;
    }

    @Override
    public Iterator<org.slf4j.Marker> iterator() {
        return null;
    }

    @Override
    public boolean contains(org.slf4j.Marker other) {
        return false;
    }

    @Override
    public boolean contains(String name) {
        return false;
    }
}
