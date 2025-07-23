package dev.prodzeus.logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class MDCAdapter implements org.slf4j.spi.MDCAdapter {

    private final ThreadLocal<Map<String, String>> contextMap = ThreadLocal.withInitial(HashMap::new);

    @Override
    public void put(@NotNull final String key, @NotNull final String val) {
        contextMap.get().put(key, val);
    }

    @Nullable
    @Override
    public String get(@NotNull final String key) {
        return contextMap.get().getOrDefault(key, null);
    }

    @Override
    public void remove(@NotNull final String key) {
        contextMap.get().remove(key);
    }

    @Override
    public void clear() {
        contextMap.get().clear();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        return contextMap.get();
    }

    @Override
    public void setContextMap(Map<String, String> contextMap) {
        this.contextMap.set(contextMap);
    }

    @Override
    public void pushByKey(String key, String value) {
        // Not supported.
    }

    @Override
    public String popByKey(String key) {
        return "Not supported.";
    }

    @Override
    public Deque<String> getCopyOfDequeByKey(String key) {
        return new ArrayDeque<>(); // Not supported.
    }

    @Override
    public void clearDequeByKey(String key) {
        // Not supported.
    }
}
