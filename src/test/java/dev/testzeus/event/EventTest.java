package dev.testzeus.event;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.event.components.EventException;
import dev.prodzeus.logger.event.components.EventListener;
import dev.prodzeus.logger.event.events.log.*;
import dev.prodzeus.logger.slf4j.SLF4JProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventTest {

    SLF4JProvider getProvider() {
        final SLF4JProvider provider = SLF4JProvider.get();
        if (provider == null) fail("SLF4JProvider == null");
        return provider;
    }

    Logger getLogger() {
        final Logger logger = getProvider().getLoggerFactory().getLogger("dev.prodzeus.test.event");
        if (logger == null) fail("Logger == null");
        return logger;
    }

    @Test
    @Order(1)
    void eventCreationTest() {
        System.out.println("[EVENT] New event creation test starting...");
        assertAll(
                () -> new TraceLogEvent(getLogger(),"Test.").fire(),
                () -> new DebugLogEvent(getLogger(),"Test.").fire(),
                () -> new InfoLogEvent(getLogger(),"Test.").fire(),
                () -> new WarningLogEvent(getLogger(),"Test.").fire(),
                () -> new ErrorLogEvent(getLogger(),"Test.").fire(),
                () -> {
                    new EventException(new RuntimeException("Test."));
                    getProvider().suppressExceptions(true);
                    new EventException(new RuntimeException("Suppressed exception."));
                },
                () -> new ExceptionLogEvent("Test.")
        );
    }

    @Test
    @Order(3)
    void genericLogEventMethodsTest() {
        System.out.println("[EVENT] GenericLogEvent methods test starting...");
        final GenericLogEvent event = new InfoLogEvent(getLogger(), "Test");
        assertAll(
                () -> assertNotNull(event.getRawLog()),
                () -> assertNotNull(event.getFormattedLog()),
                () -> assertNotNull(event.getFormattedLogNoColor()),
                () -> assertNotNull(event.getArguments()),
                () -> assertNotNull(event.getMarkers()),
                () -> assertNotNull(event.getLevel()),
                () -> assertNotNull(event.getCaller())
        );
    }

    @Test
    @Order(2)
    void newEventListenerTest() {
        System.out.println("[EVENT] New listener test starting...");
        assertAll(this::listenerTest);
    }

    void listenerTest() {
        try {
            final EventListener listener = new Listener(getLogger());
            getProvider().registerListener(listener);

            getLogger().info("INFO Test.");
            getLogger().trace("TRACE Test.");
            getLogger().warn("WARN Synchronized Test.");
        } catch (Exception e) {
            fail(e);
        }
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
