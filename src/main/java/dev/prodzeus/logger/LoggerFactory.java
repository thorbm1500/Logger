package dev.prodzeus.logger;

import org.jetbrains.annotations.NotNull;
import org.slf4j.ILoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class LoggerFactory implements ILoggerFactory {

    private static final ConcurrentHashMap<String, Logger> loggers = new ConcurrentHashMap<>();

    /**
     * Gets the Logger with the given name. If no Loggers are present of the given name,
     * a new Logger instance will be created and returned.
     * @param name The name of the Logger to return.
     * @return A Logger instance.
     */
    @Override
    public Logger getLogger(@NotNull final String name) {
        return loggers.computeIfAbsent(name, Logger::new);
    }
}
