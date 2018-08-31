package ome.services.util;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * {@link Executor} which will block the submitting thread until there is
 * space in the underlying queue.
 */
public class BoundedExecutor implements java.util.concurrent.Executor {

    private final ThreadPoolExecutor delegate;

    private final Semaphore maxTasks;

    public BoundedExecutor(ThreadPoolExecutor thread, int maxTasksInQueue) {
        this.delegate = thread;
        this.maxTasks = new Semaphore(maxTasksInQueue);
    }

    @Override
    public void execute(Runnable command) {
        try {
            if (!maxTasks.tryAcquire(1, 1, TimeUnit.HOURS)) {
                throw new RejectedExecutionException(String.format(
                    "Failed to execute %s after 1h", command
                ));
            }
        } catch (InterruptedException e) {
            throw new RejectedExecutionException(String.format(
                "Interrupted while waiting to execute %s", command
            ));
        }
        delegate.execute(command);
    }

}