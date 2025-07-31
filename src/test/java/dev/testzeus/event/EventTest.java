package dev.testzeus.event;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.SLF4JProvider;
import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.events.log.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void eventCreationTest() {
        final Logger logger = SLF4JProvider.getInstance().getLoggerFactory().getLogger("dev.prodzeus.test");
        assertAll(
                () -> assertThrows(EventException.class, () -> new EventException("Test.")),
                () -> new TraceLogEvent(logger,"Test."),
                () -> new DebugLogEvent(logger,"Test."),
                () -> new InfoLogEvent(logger,"Test."),
                () -> new WarningLogEvent(logger,"Test."),
                () -> new ErrorLogEvent(logger,"Test.")
        );
    }

    @Test
    void genericLogEventMethodsTest() {
        final Logger logger = SLF4JProvider.getInstance().getLoggerFactory().getLogger("dev.prodzeus.test");
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

}
