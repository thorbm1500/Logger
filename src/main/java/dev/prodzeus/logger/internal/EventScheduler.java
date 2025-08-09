package dev.prodzeus.logger.internal;

import dev.prodzeus.logger.Logger;
import dev.prodzeus.logger.SLF4JProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.concurrent.*;

/**
 * @apiNote <b>For internal use only.</b>
 */
public final class EventScheduler {

    private EventScheduler() {}

    private static final Logger LOGGER = SLF4JProvider.get().getLoggerFactory().getLogger("dev.prodzeus.logger.internal.EventScheduler");

    private static final Deque<Runnable> batch = new ConcurrentLinkedDeque<>();

    private static volatile int offset = 0;
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private static synchronized void executeBatch() {
        if (batch.isEmpty()) {
            offset = Math.max(offset + 25, 3000);
            return;
        } else offset = 0;
        Runnable event;
        while ((event = batch.poll()) != null) {
            executor.execute(event);
        }
    }

    public static synchronized void addToBatch(@NotNull final Runnable event) {
        final boolean schedule = batch.isEmpty();
        try {
            batch.offer(event);
        } catch (Exception e) {
            LOGGER.exceptionSynchronized(e);
        }
        if (schedule && !batch.isEmpty()) {
            scheduledExecutorService.schedule(EventScheduler::executeBatch, offset, TimeUnit.MILLISECONDS);
        }
    }
}
