package dev.prodzeus.logger.slf4j;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.components.Level;
import dev.prodzeus.logger.event.Event;
import dev.prodzeus.logger.event.EventManager;
import dev.prodzeus.logger.event.components.EventListener;
import dev.prodzeus.logger.event.events.exception.ExceptionEvent;
import dev.prodzeus.logger.event.events.log.ExceptionLogEvent;
import dev.prodzeus.logger.event.events.log.GenericLogEvent;
import dev.prodzeus.logger.event.events.log.WarningLogEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.spi.SLF4JServiceProvider;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Thor B.<br>
 * See more <i><a href="https://github.com/thorbm1500">github@thorbm1500</a></i>
 * @version 1.0.0
 */
public final class SLF4JProvider implements SLF4JServiceProvider {

    private static Level globalLevel = Level.INFO;

    private static SLF4JProvider instance;
    private static SysLogger system;
    private static DefaultListener defaultListener;
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
        if (instance == null) instance = this;
        if (system == null) system = new SysLogger("dev.prodzeus.logger");
        initialize();
        try {
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> new Thread(() -> new ExceptionEvent(throwable)));
        } catch (Exception ignored) {}
    }

    /**
     * Get the SLF4J Provider. If an instance is not present, one will be created for you.
     * @return The SLF4J Provider.
     */
    public static SLF4JProvider get() {
        synchronized (SLF4JProvider.class) {
            if (instance == null) new SLF4JProvider().initialize();
            return instance;
        }
    }

    public static @NotNull Logger getSystem() {
        return system;
    }

    public static void setGlobalLevel(@NotNull final Level level) {
        globalLevel = level;
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

    public Logger getLogger(@NotNull final String name) {
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
        if (defaultListener == null) {
            defaultListener = new DefaultListener(system);
            try {
                if (!eventManager.registerListener(defaultListener)) System.out.println("Failed to register listener. Listener is already registered!");
            } catch (Exception e) {
                System.out.println("SLF4JProvider initialization failed to register default listener! No logs will be printed!");
                e.printStackTrace();
            }
        }
        initialized = true;
        system.info("SLF4JProvider initialized.");
    }

    public boolean registerListener(final EventListener listener) {
        if (eventManager.registerListener(listener)) {
            system.info("{} is now registered to {}",listener.getClass().getSimpleName(),listener.getOwner().getName());
            return true;
        } else {
            system.info("This Listener has already been registered!");
            return false;
        }
    }

    public boolean unregisterListener(final EventListener listener) {
        if (eventManager.unregisterListener(listener)) {
            system.info("{} is no longer registered.",listener.getClass().getSimpleName());
            return true;
        }
        return false;
    }

    private static final class SysLogger extends Logger implements Serializable {

        @Serial
        private static final long serialVersionUID = 215951535491651L;

        public SysLogger(@NotNull String name) {
            super(name);
        }

        @Override
        public Logger setLevel(@NotNull final Level level) {
            this.level = level;
            defaultListener.updateLogLevel(level);
            return this;
        }
    }

    private static final class DefaultListener extends EventListener {

        DefaultListener(@NotNull final SysLogger logger) {
            super(logger);
        }
        private Level level = Level.INFO;

        public void updateLogLevel(@NotNull final Level level) {
            this.level = level;
        }

        @Override
        public void onGenericEvent(@NotNull final Event event) {
            if (event.isException() && suppressExceptions) {
                if (suppressedExceptionNotification) {
                    new WarningLogEvent(getSystem(), "Notification: " + event.getCause().getClass().getSimpleName() + " detected.");
                }
            } else if (event.getLevel().isLoggable(level)) {
                System.out.println(event.getMessage());
                System.out.flush();
            }
        }
    }
}