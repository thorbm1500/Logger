package dev.prodzeus.logger.slf4j;

import org.slf4j.IMarkerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class MarkerFactory implements IMarkerFactory {

    private static final ConcurrentHashMap<String, Marker> markers = new ConcurrentHashMap<>();

    @Override
    public Marker getMarker(String name) {
        return markers.computeIfAbsent(name, Marker::new);
    }

    @Override
    public boolean exists(String name) {
        return markers.containsKey(name);
    }

    @Override
    public boolean detachMarker(String name) {
        return markers.remove(name) != null;
    }

    @Override
    public Marker getDetachedMarker(String name) {
        return markers.getOrDefault(name, null);
    }
}
