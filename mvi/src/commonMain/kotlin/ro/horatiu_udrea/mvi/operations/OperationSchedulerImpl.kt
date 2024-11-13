package ro.horatiu_udrea.mvi.operations

import kotlinx.coroutines.*

/**
 * A component that schedules operations with keys of type [K].
 * All methods are thread-safe.
 *
 * @param syncDispatcher The coroutine dispatcher used to synchronize operations.
 */
public class OperationSchedulerImpl<in K>(syncDispatcher: CoroutineDispatcher) : OperationScheduler<K> {

    /**
     * The dispatcher used to synchronize the operations.
     */
    private val safeSyncDispatcher = syncDispatcher.limitedParallelism(1)

    /**
     * A map that holds the current jobs for each key.
     */
    private val jobMap: MutableMap<K, Jobs> = mutableMapOf()

    /**
     * Runs a coroutine if there is no other running for the given key.
     */
    override suspend fun runIfNotRunning(
        key: K,
        block: suspend () -> Unit
    ) {
        coroutineScope {
            val parentScope = this
            withContext(safeSyncDispatcher) {
                val jobs = jobMap[key]
                if (jobs == null) {
                    // launching in parent scope
                    parentScope.launchLazyCoroutine(key, block).also {
                        jobMap[key] = Jobs(executingJob = it, pendingJob = null)
                        it.start()
                    }
                }
            }
        }
    }

    /**
     * Waits for the current coroutine for the given key to finish (if any)
     * and runs a new one afterward.
     */
    override suspend fun runAfterCurrent(
        key: K,
        block: suspend () -> Unit
    ) {
        coroutineScope {
            val parentScope = this
            withContext(safeSyncDispatcher) {
                val jobs = jobMap[key]
                if (jobs == null) {
                    // launching in parent scope
                    parentScope.launchLazyCoroutine(key, block).also {
                        jobMap[key] = Jobs(executingJob = it, pendingJob = null)
                        it.start()
                    }
                } else {
                    val (executingJob, pendingJob) = jobs
                    // launching in parent scope
                    val newJob = parentScope.launchLazyCoroutine(key, block)
                    pendingJob?.cancel("New job was registered as pending", cause = null)
                    jobMap[key] = Jobs(executingJob, newJob)
                }
            }
        }
    }

    /**
     * Cancels the current coroutine for the given key (if any) and runs a new one.
     */
    override suspend fun cancelCurrentThenRun(
        key: K,
        block: suspend () -> Unit
    ) {
        coroutineScope {
            val parentScope = this
            withContext(safeSyncDispatcher) {
                val jobs = jobMap[key]
                if (jobs == null) {
                    // launching in parent scope
                    parentScope.launchLazyCoroutine(key, block).also {
                        jobMap[key] = Jobs(executingJob = it, pendingJob = null)
                        it.start()
                    }
                } else {
                    val (executingJob, pendingJob) = jobs
                    // launching in parent scope
                    val newJob = parentScope.launchLazyCoroutine(key, block)
                    pendingJob?.cancel("New job was registered as pending", cause = null)
                    jobMap[key] = Jobs(executingJob, newJob)
                    executingJob.cancel("Cancelling in favor of new job", cause = null)
                }
            }
        }
    }

    /**
     * Cancels the current coroutine for the given key (if any).
     * Does not wait for the job to finish its execution.
     */
    override suspend fun cancel(key: K) {
        withContext(safeSyncDispatcher) {
            val (executingJob, pendingJob) = jobMap[key] ?: return@withContext
            if (pendingJob != null) {
                pendingJob.cancel()
                jobMap[key] = Jobs(executingJob, pendingJob = null)
            }
            executingJob.cancel()
        }
    }

    private fun CoroutineScope.launchLazyCoroutine(
        key: K,
        block: suspend () -> Unit,
    ) = launch(start = CoroutineStart.LAZY) {
        try {
            block()
        } finally {
            val currentJob = coroutineContext.job
            withContext(safeSyncDispatcher + NonCancellable) {
                val (executingJob, pendingJob) = jobMap[key]
                    ?: error("Job was removed from map before cancellation")

                if (executingJob != currentJob)
                    error("Job was replaced before cancellation")

                if (pendingJob != null) {
                    jobMap[key] = Jobs(executingJob = pendingJob, pendingJob = null)
                    pendingJob.start()
                } else {
                    jobMap.remove(key)
                }
            }
        }
    }
}

/**
 * A class that holds the current executing job and the pending job for a given key.
 */
private data class Jobs(val executingJob: Job, val pendingJob: Job?)