package org.mvplugins.multiverse.inventories.profile.bulkedit;

import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.atomic.AtomicInteger;

@ApiStatus.Experimental
public final class BulkEditResult {

    private final long startTime = System.nanoTime();
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);

    BulkEditResult() { }

    void incrementSuccess() {
        successCount.incrementAndGet();
    }

    void incrementFailure() {
        failureCount.incrementAndGet();
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getFailureCount() {
        return failureCount.get();
    }

    /**
     * In milliseconds
     *
     * @return Gets the time taken
     */
    public double getTimeTaken() {
        return (double) (System.nanoTime() - startTime) / 1_000_000.0;
    }
}
