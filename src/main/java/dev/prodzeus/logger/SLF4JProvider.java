package dev.prodzeus.logger;

import dev.prodzeus.logger.components.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * Simple Logger with SLF4J implementation, and quality of life features,
 * <i>such as; Automatic formatting of exceptions, Event listeners, and more.</i>
 * @author Thor B.<br>
 * See more <i><a href="https://github.com/thorbm1500">github@thorbm1500</a></i><br>
 * See more <i><a href="https://repo.prodzeus.dev">repo@prodzeus</a></i>
 * @version 1.0.1
 */
public final class SLF4JProvider implements SLF4JServiceProvider {

    private static Level globalLevel = Level.INFO;

    private static SLF4JProvider instance;
    private static final Logger system;

    private static final LoggerFactory loggerFactory;
    private static final MarkerFactory markerFactory;
    private static final MDCAdapter adapter;

    static {
        loggerFactory = new LoggerFactory();
        markerFactory = new MarkerFactory();
        adapter = new MDCAdapter();

        system = loggerFactory.getLogger(Logger.class.getName());
    }

    /**
     * @apiNote <b>It's highly recommended to use the {@link SLF4JProvider#get()} method</b>,
     *          instead of constructing a new instance yourself, as it
     *          allows you to have a static SLF4J Provider, across your entire project.
     *          The {@link SLF4JProvider#get()} method will create a new instance, if one isn't present.
     *          This method is only included to comply with the standard SLF4J implementation, and allows
     *          other systems to make use of this Logger.
     * @see SLF4JProvider#get()
     */
    public SLF4JProvider() {
        if (instance == null) instance = this;
        try {
            Thread.setDefaultUncaughtExceptionHandler(
                    (thread, throwable) -> new Thread(() -> system.error("Unhandled exception caught in {}! \n{}", thread.getName(), throwable)).start());
        } catch (Exception ignored) {}
    }

    /**
     * Gets the SLF4J Provider. If an instance is not present, one will be created for you.
     * @return The SLF4J Provider.
     */
    @Contract(pure = true)
    public static @NotNull SLF4JProvider get() {
        synchronized (SLF4JProvider.class) {
            if (instance == null) new SLF4JProvider();
            return instance;
        }
    }

    /**
     * For internal logging.
     * @return Logger instance.
     */
    @Contract(pure = true)
    static @NotNull Logger getSystem() {
        return system;
    }

    /**
     * Sets the Global Log level.
     * <p>
     *      This level has a higher priority than the Loggers'
     *      own level, allowing you to force all Loggers to a desired level.
     *      This can be <i>turned off</i> on the individual Logger if you wish to have separate log levels.
     * </p>
     * @param level The new Global level.
     * @see Logger#ignoreGlobalLogLevel(boolean) 
     */
    public static void setGlobalLevel(@NotNull final Level level) {
        globalLevel = level;
    }

    /**
     * Gets the current Global level.
     * @return The Global level.
     * @see SLF4JProvider#setGlobalLevel(Level) 
     */
    @Contract(pure = true)
    public static @NotNull Level getGlobalLevel() {
        return globalLevel;
    }

    /**
     * Gets the Logger factory.
     * @return The Logger factory.
     */
    @Override @Contract(pure = true)
    public @NotNull LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    /**
     * Gets the Marker factory.
     * @return The Marker factory.
     */
    @Override @Contract(pure = true)
    public @NotNull MarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    /**
     * @apiNote <b><i>This feature is not supported!</i></b>
     */
    @Override @Contract(pure = true)
    public @NotNull MDCAdapter getMDCAdapter() {
        return adapter;
    }

    /**
     * Gets the SLF4J API version implemented.
     * @return String of the API version.
     */
    @Override @Contract(pure = true)
    public @NotNull String getRequestedApiVersion() {
        return "2.0.12";
    }

    @Override
    public void initialize() {
        /* Empty */
    }
}
