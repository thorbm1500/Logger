package dev.prodzeus.logger;

import org.slf4j.spi.SLF4JServiceProvider;

public class SLF4JProvider implements SLF4JServiceProvider {

    private final LoggerFactory loggerFactory = new LoggerFactory();
    private final MarkerFactory markerFactory = new MarkerFactory();
    private final MDCAdapter adapter = new MDCAdapter();

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

    @Override
    public String getRequestedApiVersion() {
        return "2.0.12";
    }

    @Override
    public void initialize() { /* Empty */ }
}