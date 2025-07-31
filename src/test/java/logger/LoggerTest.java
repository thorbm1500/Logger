package logger;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.LoggerFactory;
import dev.prodzeus.logger.SLF4JProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    @Test
    void SLF4JProviderTest() {
        assertDoesNotThrow(() -> {
            assertAll(
                    () -> assertNotNull(SLF4JProvider.getInstance().getLoggerFactory()),
                    () -> assertNotNull(SLF4JProvider.getInstance().getMarkerFactory()),
                    () -> assertNotNull(SLF4JProvider.getInstance().getMDCAdapter()),
                    () -> assertNotNull(SLF4JProvider.getInstance().getEventManager()),
                    () -> assertNotNull(SLF4JProvider.getInstance().getRequestedApiVersion())
            );
        });
    }

    @Test
    void newLoggerTest() {
        assertNotNull(LoggerFactory.getInstance().getLogger("dev.prodzeus.test"));
    }

    @Test
    void logTest() {
        assertAll(
                () -> {
                    final Logger logger = LoggerFactory.getInstance().getLogger("dev.prodzeus.test");
                    logger.trace("Test.");
                    logger.trace("Test. {}", "Test");
                    logger.debug("Test.");
                    logger.debug("Test. {}", "Test");
                    logger.info("Test.");
                    logger.info("Test. {}", "Test");
                    logger.warn("Test.");
                    logger.warn("Test. {}", "Test");
                    logger.error("Test.");
                    logger.error("Test. {}", "Test");
                }
        );
    }
}
