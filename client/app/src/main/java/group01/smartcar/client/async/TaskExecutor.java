package group01.smartcar.client.async;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class TaskExecutor {

    private static final TaskExecutor INSTANCE = new TaskExecutor();

    private static final int DEFAULT_WAIT_TIME = 100;
    private static final int DEFAULT_SERVICE_TIME = 5;
    private static final int BLOCKING_COEFFICIENT = DEFAULT_WAIT_TIME / DEFAULT_SERVICE_TIME;

    private final ScheduledExecutorService scheduledExecutorService;

    private TaskExecutor() {
        scheduledExecutorService = Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors() * (1 + BLOCKING_COEFFICIENT)
        );
    }

    public ScheduledFuture<?> scheduleTask(Runnable runnable) {
        return scheduleTask(runnable, DEFAULT_WAIT_TIME);
    }

    public ScheduledFuture<?> scheduleTask(Runnable runnable, int delay) {
        return scheduledExecutorService.scheduleAtFixedRate(runnable, 0, delay, TimeUnit.MILLISECONDS);
    }

    public static TaskExecutor getInstance() {
        return INSTANCE;
    }
}
