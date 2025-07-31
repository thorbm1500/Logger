package dev.prodzeus.logger;

import org.jetbrains.annotations.NotNull;
import org.slf4j.ILoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public final class LoggerFactory implements ILoggerFactory {

    private static LoggerFactory instance;

    private static final ConcurrentHashMap<String, Logger> loggers = new ConcurrentHashMap<>(16);

    private LoggerFactory() {
        instance = this;
    }

    public static synchronized LoggerFactory getInstance() {
        return instance == null ? new LoggerFactory() : instance;
    }

    /**
     * Gets the Logger with the given name. If no Loggers are present of the given name,
     * a new Logger instance will be created and returned.
     *
     * @param name The name of the Logger to return.
     * @return A Logger instance.
     */
    @Override
    public synchronized Logger getLogger(@NotNull final String name) {
        if (!loggers.isEmpty() && loggers.containsKey(name) && loggers.get(name) != null) {
            return loggers.get(name);
        }
        final Logger logger = new Logger(name);
        loggers.put(name, logger);
        return logger;
    }
}
