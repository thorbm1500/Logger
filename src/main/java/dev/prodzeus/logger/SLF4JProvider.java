package dev.prodzeus.logger;

import dev.prodzeus.logger.event.EventManager;
import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.components.EventHandler;
import dev.prodzeus.logger.event.components.EventListener;
import dev.prodzeus.logger.event.components.RegisteredListener;
import dev.prodzeus.logger.event.events.log.ExceptionLogEvent;
import dev.prodzeus.logger.event.events.log.GenericLogEvent;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * @author Thor B.<br>
 * See more <i><a href="https://github.com/thorbm1500">github@thorbm1500</a></i>
 * @version 1.0.0
 */
public final class SLF4JProvider implements SLF4JServiceProvider {

    private static SLF4JProvider instance;
    private static Logger system;
    private static boolean initialized = false;
    private static boolean suppressExceptions = false;
    private static boolean suppressedExceptionNotification = true;

    private static final EventManager eventManager;
    private static final LoggerFactory loggerFactory;
    private static final MarkerFactory markerFactory;
    private static final MDCAdapter adapter;

    static {
        eventManager = new EventManager();
        loggerFactory = new LoggerFactory();
        markerFactory = new MarkerFactory();
        adapter = new MDCAdapter();
    }

    /**
     * @apiNote It's recommended to use {@link SLF4JProvider#get()},
     *          instead of constructing a new instance yourself, which
     *          allows you to have a static SLF4J Provider, across your entire project.
     * @see SLF4JProvider#get()
     */
    public SLF4JProvider() {
        instance = this;
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> new EventException(throwable));
    }

    /**
     * Get the SLF4J Provider. If an instance is not present, one will be created for you.
     * @return The SLF4J Provider.
     */
    public static SLF4JProvider get() {
        synchronized (SLF4JProvider.class) {
            if (instance == null) new SLF4JProvider().initialize();
            if (!initialized) instance.initialize();
            return instance;
        }
    }

    public static Logger getSystem() {
        if (system == null) {
            if (instance == null) get();
            else createSystemLogger();
        }
        return system;
    }

    private static void createSystemLogger() {
        synchronized (SLF4JProvider.class) {
            system = loggerFactory.getLogger("dev.prodzeus.logger");
        }
    }

    /**
     * Suppress exceptions from being logged.
     * @param suppress True, if exceptions should be ignored, otherwise false.
     * @apiNote Default → <b><i>False</i></b>
     * @see SLF4JProvider#suppressExceptions(boolean, boolean)
     */
    public void suppressExceptions(final boolean suppress) {
        suppressExceptions = suppress;
    }

    /**
     * Suppress exceptions from being logged.
     * @param suppress True, if exceptions should be ignored, otherwise false.
     * @param notify   True, if a notification should be logged, when an
     *                 exception occurs while {@code suppress} is set to true.
     * @apiNote Default → <b><i>False</i></b>
     */
    public void suppressExceptions(final boolean suppress, final boolean notify) {
        suppressExceptions = suppress;
        notifyOfSuppressedExceptions(notify);
    }

    /**
     * Whether a notification should be logged with Log Level {@link Level#EXCEPTION},
     * when an exception is thrown and {@link SLF4JProvider#suppressExceptions} is set to true.
     * @param notify True, if notifications should be logged, otherwise false.
     * @apiNote Default → <b><i>True</i></b>
     * @see SLF4JProvider#suppressExceptions
     */
    public void notifyOfSuppressedExceptions(final boolean notify) {
        suppressedExceptionNotification = notify;
    }

    public static Logger getLogger(@NotNull final String name) {
        return loggerFactory.getLogger(name);
    }

    @Override
    public LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public MarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return adapter;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public String getRequestedApiVersion() {
        return "2.0.12";
    }

    @Override
    public void initialize() {
        if (initialized) return;
        initialized = true;

        try {
            eventManager.registerListener(new DefaultListener(), getSystem());
        } catch (Exception e) {
            System.out.println("SLF4JProvider initialization failed! Failed to register default listener! No logs will be printed!");
            e.printStackTrace();
            return;
        }

        getSystem().info("SLF4JProvider initialized.");
    }

    @SneakyThrows
    public boolean registerListener(final EventListener listener, final Logger logger) {
        final RegisteredListener newListener = eventManager.registerListener(listener,logger);
        if (newListener == null) {
            getSystem().info("{} already has this Listener type registered!",logger.getName());
            return false;
        } else {
            getSystem().info("{} is now registered to {}",newListener.getListenerRegistered().getClass().getSimpleName(),logger.getName());
            return true;
        }
    }

    public boolean unregisterListener(final EventListener listener, final Logger logger) {
        if (eventManager.unregisterListener(listener,logger)) {
            getSystem().info("{} is no longer registered to {}",listener.getClass().getSimpleName(),logger.getName());
            return true;
        }
        return false;
    }

    public static final class DefaultListener implements EventListener {
        @EventHandler
        public void onLogEvent(@NotNull final GenericLogEvent event) {
            if (event.getException() != null && suppressExceptions) {
                if (suppressedExceptionNotification) {
                    new ExceptionLogEvent("Notification: " + event.getException().getCause().getClass().getSimpleName() + " detected.");
                }
                return;
            }
            if (event.getLevel().isLoggable(getSystem().getLevel())) System.out.println(event.getFormattedLog());
        }
    }
}