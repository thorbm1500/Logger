package dev.testzeus.logger;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.slf4j.SLF4JProvider;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoggerTest {

    SLF4JProvider getProvider() {
        final SLF4JProvider provider = SLF4JProvider.get();
        if (provider == null) fail("SLF4JProvider == null");
        return provider;
    }

    Logger getLogger() {
        final Logger logger = getProvider().getLoggerFactory().getLogger("dev.prodzeus.test.logger");
        if (logger == null) fail("Logger == null");
        return logger;
    }

    @Test
    @Order(6)
    void newSLF4JProviderTest() {
        System.out.println("[LOG] New SLF4J Provider test starting...");
        assertDoesNotThrow(this::getProvider);
    }

    @Test
    @Order(1)
    void SLF4JProviderMethodsTest() {
        System.out.println("[LOG] SLF4J Provider methods test starting...");
        assertAll(
                () -> assertNotNull(getProvider().getLoggerFactory()),
                () -> assertNotNull(getProvider().getMarkerFactory()),
                () -> assertNotNull(getProvider().getMDCAdapter()),
                () -> assertNotNull(getProvider().getEventManager()),
                () -> assertNotNull(getProvider().getRequestedApiVersion())
        );
    }

    @Test
    @Order(5)
    void newLoggerTest() {
        System.out.println("[LOG] New logger creation test starting...");
        assertDoesNotThrow(this::getLogger);
    }

    @Test
    @Order(2)
    void logTest() {
        System.out.println("[LOG] Logging test starting...");
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
    @Order(3)
    void logTestSynchronized() {
        System.out.println("[LOG] Synchronized logging test starting...");
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
    @Order(4)
    void logTestAsync() {
        System.out.println("[LOG] Async logging test starting...");
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
}
