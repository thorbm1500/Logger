package dev.prodzeus.logger;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.Map;

/**
 * @apiNote <b><i>This feature is not supported!</i></b>
 */
public class MDCAdapter implements org.slf4j.spi.MDCAdapter {

    /**
     * @apiNote <b><i>This feature is not supported!</i></b>
     */
    @Override
    public void put(@NotNull final String key, @NotNull final String val) {
        /* Empty */
    }

    /**
     * @apiNote <b><i>This feature is not supported!</i></b>
     */
    @Override @Contract(pure = true)
    public @Nullable String get(@NotNull final String key) {
        return null;
    }

    /**
     * @apiNote <b><i>This feature is not supported!</i></b>
     */
    @Override
    public void remove(@NotNull final String key) {
        /* Empty */
    }

    /**
     * @apiNote <b><i>This feature is not supported!</i></b>
     */
    @Override
    public void clear() {
        /* Empty */
    }

    /**
     * @apiNote <b><i>This feature is not supported!</i></b>
     */
    @Override @Contract(pure = true)
    public @Nullable Map<String, String> getCopyOfContextMap() {
        return null;
    }

    /**
     * @apiNote <b><i>This feature is not supported!</i></b>
     */
    @Override
    public void setContextMap(Map<String, String> contextMap) {
        /* Empty */
    }

    /**
     * @apiNote <b><i>This feature is not supported!</i></b>
     */
    @Override
    public void pushByKey(String key, String value) {
        /* Empty */
    }

    /**
     * @apiNote <b><i>This feature is not supported!</i></b>
     */
    @Override @Contract(pure = true)
    public @Nullable String popByKey(String key) {
        return null;
    }

    /**
     * @apiNote <b><i>This feature is not supported!</i></b>
     */
    @Override @Contract(pure = true)
    public @Nullable Deque<String> getCopyOfDequeByKey(String key) {
        return null;
    }

    /**
     * @apiNote <b><i>This feature is not supported!</i></b>
     */
    @Override
    public void clearDequeByKey(String key) {
        /* Empty */
    }
}
