package dev.testzeus.event;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.components.EventListener;
import dev.prodzeus.logger.event.events.log.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventTest {

    @Test
    void eventCreationTest() {
        final Logger logger = SLF4JProvider.get().getLoggerFactory().getLogger("dev.prodzeus.test");
        assertAll(
                () -> new TraceLogEvent(logger,"Test.").fire(),
                () -> new DebugLogEvent(logger,"Test.").fire(),
                () -> new InfoLogEvent(logger,"Test.").fire(),
                () -> new WarningLogEvent(logger,"Test.").fire(),
                () -> new ErrorLogEvent(logger,"Test.").fire(),
                () -> {
                    new EventException(new RuntimeException("Test."));
                    SLF4JProvider.get().suppressExceptions(true);
                    new EventException(new RuntimeException("Suppressed exception."));
                },
                () -> new ExceptionLogEvent("Test.")
        );
    }

    @Test
    void genericLogEventMethodsTest() {
        final Logger logger = SLF4JProvider.get().getLoggerFactory().getLogger("dev.prodzeus.test");
        final GenericLogEvent event = new InfoLogEvent(logger, "Test");
        assertAll(
                () -> assertNotNull(event.getRawLog()),
                () -> assertNotNull(event.getFormattedLog()),
                () -> assertNotNull(event.getFormattedLogNoColor()),
                () -> assertNotNull(event.getArguments()),
                () -> assertNotNull(event.getMarkers()),
                () -> assertNotNull(event.getLevel()),
                () -> assertNotNull(event.getCaller()),
                () -> assertNotNull(event.getRegisteredListeners())
        );
    }

    @Test
    void eventListeningTest() {
        assertAll(
                () -> {
                    final Logger logger = SLF4JProvider.get().getLoggerFactory().getLogger("dev.prodzeus.test");
                    SLF4JProvider.get().registerListener(new Listener(logger));

                    logger.info("INFO Test.");
                    logger.trace("TRACE Test.");
                    logger.warn("WARN Synchronized Test.");
                }
        );
    }

    public static class Listener extends EventListener {

        protected Listener(@NotNull Logger logger) {
            super(logger);
        }

        public void onLogMessage(@NotNull final GenericLogEvent event) {
            System.out.println("Event Listener: " + event.getFormattedLog());
        }
    }
}
