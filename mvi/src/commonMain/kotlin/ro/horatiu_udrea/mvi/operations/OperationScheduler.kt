package ro.horatiu_udrea.mvi.operations

/**
 * A component that schedules operations with keys of type [K].
 * All methods are thread-safe.
 */
public interface OperationScheduler<in K> {

    /**
     * Runs a coroutine if there is no other running for the given key.
     */
    public suspend fun runIfNotRunning(
        key: K,
        block: suspend () -> Unit
    )

    /**
     * Waits for the current coroutine for the given key to finish (if any)
     * and runs a new one afterward.
     */
    public suspend fun runAfterCurrent(
        key: K,
        block: suspend () -> Unit
    )

    /**
     * Cancels the current coroutine for the given key (if any) and runs a new one.
     */
    public suspend fun cancelCurrentThenRun(
        key: K,
        block: suspend () -> Unit
    )

    /**
     * Cancels the current coroutine for the given key (if any).
     * Does not wait for the job to finish its execution.
     */
    public suspend fun cancel(key: K)
}