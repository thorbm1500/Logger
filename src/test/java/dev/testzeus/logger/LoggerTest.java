package dev.testzeus.logger;

import dev.prodzeus.logger.Event;
import dev.prodzeus.logger.Listener;
import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.SLF4JProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoggerTest {

    SLF4JProvider getProvider() {
        return SLF4JProvider.get();
    }

    Logger getLogger() {
        return getProvider().getLoggerFactory().getLogger("dev.prodzeus.test.logger");
    }

    @Test
    void newSLF4JProviderTest() {
        assertDoesNotThrow(SLF4JProvider::new);
        assertDoesNotThrow(this::getProvider);
    }

    @Test
    void SLF4JProviderMethodsTest() {
        assertAll(
                () -> assertNotNull(getProvider().getLoggerFactory()),
                () -> assertNotNull(getProvider().getMarkerFactory()),
                () -> assertNotNull(getProvider().getMDCAdapter()),
                () -> assertNotNull(getProvider().getRequestedApiVersion())
        );
    }

    @Test
    void newLoggerTest() {
        assertDoesNotThrow(this::getLogger);
    }

    @Test
    void logTest() {
        assertAll(
                () -> getLogger().trace("Standard."),
                () -> getLogger().trace("Standard. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().trace("Standard. {} {} {}", "Placeholder", 1, true),
                () -> getLogger().debug("Standard."),
                () -> getLogger().debug("Standard. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().debug("Standard. {} {} {}", "Placeholder", 1, true),
                () -> getLogger().info("Standard."),
                () -> getLogger().info("Standard. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().info("Standard. {} {} {}", "Placeholder", 1, true),
                () -> getLogger().warn("Standard."),
                () -> getLogger().warn("Standard. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().warn("Standard. {} {} {}", "Placeholder", 1, true),
                () -> getLogger().error("Standard."),
                () -> getLogger().error("Standard. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().error("Standard. {} {} {}", "Placeholder", 1, true)
        );
    }

    @Test
    void logTestSynchronized() {
        assertAll(
                () -> getLogger().traceSynchronized("Synchronized."),
                () -> getLogger().traceSynchronized("Synchronized. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().traceSynchronized("Synchronized. {} {} {}", "Placeholder", 1, true),
                () -> getLogger().debugSynchronized("Synchronized."),
                () -> getLogger().debugSynchronized("Synchronized. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().debugSynchronized("Synchronized. {} {} {}", "Placeholder", 1, true),
                () -> getLogger().infoSynchronized("Synchronized."),
                () -> getLogger().infoSynchronized("Synchronized. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().infoSynchronized("Synchronized. {} {} {}", "Placeholder", 1, true),
                () -> getLogger().warnSynchronized("Synchronized."),
                () -> getLogger().warnSynchronized("Synchronized. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().warnSynchronized("Synchronized. {} {} {}", "Placeholder", 1, true),
                () -> getLogger().errorSynchronized("Synchronized."),
                () -> getLogger().errorSynchronized("Synchronized. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().errorSynchronized("Synchronized. {} {} {}", "Placeholder", 1, true)
        );
    }

    @Test
    void logTestAsync() {
        assertAll(
                () -> getLogger().traceAsync("Async."),
                () -> getLogger().traceAsync("Async. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().traceAsync("Async. {} {} {}", "Placeholder", 1, true),
                () -> getLogger().debugAsync("Async."),
                () -> getLogger().debugAsync("Async. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().debugAsync("Async. {} {} {}", "Placeholder", 1, true),
                () -> getLogger().infoAsync("Async."),
                () -> getLogger().infoAsync("Async. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().infoAsync("Async. {} {} {}", "Placeholder", 1, true),
                () -> getLogger().warnAsync("Async."),
                () -> getLogger().warnAsync("Async. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().warnAsync("Async. {} {} {}", "Placeholder", 1, true),
                () -> getLogger().errorAsync("Async."),
                () -> getLogger().errorAsync("Async. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> getLogger().errorAsync("Async. {} {} {}", "Placeholder", 1, true)
        );
    }

    @Test
    void markerTest() {
        assertAll(
                () -> getLogger().trace(getProvider().getMarkerFactory().getMarker("Test Marker"), "Marker test."),
                () -> getLogger().debug(getProvider().getMarkerFactory().getMarker("Test Marker"), "Marker test."),
                () -> getLogger().info(getProvider().getMarkerFactory().getMarker("Test Marker"), "Marker test."),
                () -> getLogger().warn(getProvider().getMarkerFactory().getMarker("Test Marker"), "Marker test."),
                () -> getLogger().error(getProvider().getMarkerFactory().getMarker("Test Marker"), "Marker test.")
        );
    }

    @Test
    void exceptionLoggingTest() {
        assertAll(
                () -> getLogger().trace("Exception Test. {}", new RuntimeException("Exception example.")),
                () -> getLogger().debug("Exception Test. {}", new RuntimeException("Exception example.")),
                () -> getLogger().info("Exception Test. {}", new RuntimeException("Exception example.")),
                () -> getLogger().warn("Exception Test. {}", new RuntimeException("Exception example.")),
                () -> getLogger().error("Exception Test. {}", new RuntimeException("Exception example.")),
                () -> getLogger().info("Exception Test.", new RuntimeException("Exception example, with no placeholder."))
        );
    }

    @Test
    void newListenerTest() {
        assertDoesNotThrow(() -> {
            final Listener listener = new TestListener(getLogger()).register();
            getLogger().info("Listener test log for manual verification.");
            Thread.sleep(500);
        });
    }

    static class TestListener extends Listener {

        protected TestListener(@NotNull final Logger logger) {
            super(logger);
        }

        @Override
        public void onInfoLogEvent(@NotNull final Event event) {
            System.out.println("From Listener: " + event.getMessage());
        }
    }
}
