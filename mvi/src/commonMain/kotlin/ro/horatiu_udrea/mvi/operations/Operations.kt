package ro.horatiu_udrea.mvi.operations

/**
 * A component that schedules operations with keys of type [K].
 */
public interface Operations<in K> {

    /**
     * Runs a coroutine if there is no other running for the given key.
     * The method is thread-safe.
     */
    public suspend fun runIfNotRunning(
        key: K,
        block: suspend () -> Unit
    )

    /**
     * Waits for the current coroutine for the given key to finish (if any)
     * and runs a new one afterward.
     * The method is thread-safe.
     */
    public suspend fun runAfterCurrent(
        key: K,
        block: suspend () -> Unit
    )

    /**
     * Cancels the current coroutine for the given key (if any) and runs a new one.
     * The method is thread-safe.
     */
    public suspend fun cancelCurrentAndRun(
        key: K,
        block: suspend () -> Unit
    )

    /**
     * Cancels the current coroutine for the given key (if any).
     * Does not wait for the job to finish its execution.
     * The method is thread-safe.
     */
    public suspend fun cancel(key: K)
}