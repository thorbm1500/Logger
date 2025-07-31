package dev.prodzeus.logger;

import dev.prodzeus.logger.event.EventManager;
import dev.prodzeus.logger.event.components.EventListener;
import org.slf4j.spi.SLF4JServiceProvider;

public class SLF4JProvider implements SLF4JServiceProvider {

    private static SLF4JProvider instance;

    /**
     * @apiNote It's recommended to use {@link SLF4JProvider#getInstance()},
     *          instead of constructing a new instance yourself, which
     *          allows you to have a static SLF4J Provider, across your entire project.
     * @see SLF4JProvider#getInstance()
     */
    public SLF4JProvider() {
        instance = this;
    }

    public static synchronized SLF4JProvider getInstance() {
        return instance == null ? new SLF4JProvider() : instance;
    }

    public static synchronized Logger getSystem() {
        return getInstance().getLoggerFactory().getLogger("dev.prodzeus.logger");
    }

    private final MarkerFactory markerFactory = new MarkerFactory();
    private final MDCAdapter adapter = new MDCAdapter();

    @Override
    public LoggerFactory getLoggerFactory() {
        return LoggerFactory.getInstance();
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
        return EventManager.getInstance();
    }

    @Override
    public String getRequestedApiVersion() {
        return "2.0.12";
    }

    @Override
    public void initialize() {
        getSystem().setLevel(Level.INFO);
        getSystem().info("SLF4JProvider initialized.");
    }

    public boolean registerListener(final EventListener listener, final Logger logger) {
        if (EventManager.registerListener(listener,logger)) {
            getSystem().info("New Event Listener registered for {}.", logger.getName());
            return true;
        }
        return false;
    }

    public boolean unregisterListener(final EventListener listener, final Logger logger) {
        if (EventManager.unregisterListener(listener,logger)) {
            getSystem().info("Event Listener unregistered for {}.", logger.getName());
            return true;
        }
        return false;
    }
}