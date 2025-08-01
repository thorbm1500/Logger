package dev.testzeus.logger;

import dev.prodzeus.logger.Level;
import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.SLF4JProvider;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoggerTest {

    @Test
    @Order(1)
    void newSLF4JProviderTest() {
        assertDoesNotThrow(() -> SLF4JProvider.getSystem().setLevel(Level.INFO));
    }

    @Test
    void SLF4JProviderMethodsTest() {
        assertAll(
                () -> assertNotNull(SLF4JProvider.get().getLoggerFactory()),
                () -> assertNotNull(SLF4JProvider.get().getMarkerFactory()),
                () -> assertNotNull(SLF4JProvider.get().getMDCAdapter()),
                () -> assertNotNull(SLF4JProvider.get().getEventManager()),
                () -> assertNotNull(SLF4JProvider.get().getRequestedApiVersion())
        );
    }

    @Test
    void newLoggerTest() {
        assertAll(() -> SLF4JProvider.get().getLoggerFactory().getLogger("dev.prodzeus.test"));
    }

    @Test
    void logTest() {
        final Logger logger = SLF4JProvider.get().getLoggerFactory().getLogger("dev.prodzeus.test");
        assertAll(
                () -> logger.trace("Test."),
                () -> logger.trace("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.trace("Test. {} {} {}", "Placeholder", 1, true),
                () -> logger.debug("Test."),
                () -> logger.debug("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.debug("Test. {} {} {}", "Placeholder", 1, true),
                () -> logger.info("Test."),
                () -> logger.info("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.info("Test. {} {} {}", "Placeholder", 1, true),
                () -> logger.warn("Test."),
                () -> logger.warn("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.warn("Test. {} {} {}", "Placeholder", 1, true),
                () -> logger.error("Test."),
                () -> logger.error("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.error("Test. {} {} {}", "Placeholder", 1, true)
        );
    }

    @Test
    void logTestSynchronized() {
        final Logger logger = SLF4JProvider.get().getLoggerFactory().getLogger("dev.prodzeus.test");
        assertAll(
                () -> logger.traceSynchronized("Test."),
                () -> logger.traceSynchronized("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.traceSynchronized("Test. {} {} {}", "Placeholder", 1, true),
                () -> logger.debugSynchronized("Test."),
                () -> logger.debugSynchronized("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.debugSynchronized("Test. {} {} {}", "Placeholder", 1, true),
                () -> logger.infoSynchronized("Test."),
                () -> logger.infoSynchronized("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.infoSynchronized("Test. {} {} {}", "Placeholder", 1, true),
                () -> logger.warnSynchronized("Test."),
                () -> logger.warnSynchronized("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.warnSynchronized("Test. {} {} {}", "Placeholder", 1, true),
                () -> logger.errorSynchronized("Test."),
                () -> logger.errorSynchronized("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.errorSynchronized("Test. {} {} {}", "Placeholder", 1, true)
        );
    }

    @Test
    void logTestAsync() {
        final Logger logger = SLF4JProvider.get().getLoggerFactory().getLogger("dev.prodzeus.test");
        assertAll(
                () -> logger.traceAsync("Test."),
                () -> logger.traceAsync("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.traceAsync("Test. {} {} {}", "Placeholder", 1, true),
                () -> logger.debugAsync("Test."),
                () -> logger.debugAsync("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.debugAsync("Test. {} {} {}", "Placeholder", 1, true),
                () -> logger.infoAsync("Test."),
                () -> logger.infoAsync("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.infoAsync("Test. {} {} {}", "Placeholder", 1, true),
                () -> logger.warnAsync("Test."),
                () -> logger.warnAsync("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.warnAsync("Test. {} {} {}", "Placeholder", 1, true),
                () -> logger.errorAsync("Test."),
                () -> logger.errorAsync("Test. @black black@reset @red red@reset @blue blue@reset reset"),
                () -> logger.errorAsync("Test. {} {} {}", "Placeholder", 1, true)
        );
    }
}
