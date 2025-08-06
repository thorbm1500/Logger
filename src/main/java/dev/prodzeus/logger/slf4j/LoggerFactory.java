package dev.prodzeus.logger.slf4j;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.event.components.EventException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.ILoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public final class LoggerFactory implements ILoggerFactory {

    private static final ConcurrentHashMap<String, Logger> loggers = new ConcurrentHashMap<>(16);
    private String registering = "dev.prodzeus.logger";

    public LoggerFactory() {
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
        registering = name;
        final Logger logger = new Logger(name);
        synchronized (loggers) {
            loggers.put(name, logger);
            registering = "";
        }
        return logger;
    }

    public synchronized void validate(@NotNull final Logger logger) {
        if (!registering.equals(logger.getName())) {
            new EventException(new IllegalAccessException("Manual logger creation detected! "+logger.getName()+" was created outside of the Logger Factory!"));
            synchronized (loggers) {
                loggers.put(logger.getName(), logger);
            }
        }
    }
}
