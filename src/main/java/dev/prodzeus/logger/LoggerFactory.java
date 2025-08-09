package dev.prodzeus.logger;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.ILoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The Logger Factory is responsible for handling all existing Loggers and for creating new Loggers.
 * @apiNote Factory operations are Thread safe.
 */
public final class LoggerFactory implements ILoggerFactory {

    private final ConcurrentHashMap<String, Logger> loggers = new ConcurrentHashMap<>(16);

    /**
     * Gets the Logger with the given name.
     * If no Loggers are present with the given name,
     * a new Logger will be created and returned.
     * @param name The name of the Logger.
     * @return The existing or new Logger.
     */
    @Override @Contract(pure = true)
    public synchronized Logger getLogger(@NotNull final String name) {
        return loggers.computeIfAbsent(name, Logger::new);
    }
}
