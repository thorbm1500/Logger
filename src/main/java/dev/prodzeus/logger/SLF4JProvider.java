package dev.prodzeus.logger;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public class SLF4JProvider implements SLF4JServiceProvider {

    private final ILoggerFactory loggerFactory = new ZLoggerFactory();
    private final IMarkerFactory markerFactory = new MarkerFactory();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return null;
    }

    @Override
    public String getRequestedApiVersion() {
        return "2.0.12";
    }

    @Override
    public void initialize() {}
}
