package ro.horatiu_udrea.mvi.operations

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A component that schedules operations with keys of type [K].
 * All methods are thread-safe.
 */
internal class OperationSchedulerImpl<in K> : OperationScheduler<K> {

    /**
     * A map that holds the current jobs for each key.
     */
    private val jobMap: MutableMap<K, Jobs> = mutableMapOf()

    /**
     * Mutex to serialize access to the job map
     */
    private val mutex = Mutex()

    /**
     * Runs a coroutine if there is no other running for the given key.
     */
    override suspend fun runIfNotRunning(
        key: K,
        block: suspend () -> Unit
    ) {
        coroutineScope {
            mutex.withLock {
                val jobs = jobMap[key]
                if (jobs == null) {
                    launchLazyCoroutine(key, block).also {
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
            mutex.withLock {
                val jobs = jobMap[key]
                if (jobs == null) {
                    launchLazyCoroutine(key, block).also {
                        jobMap[key] = Jobs(executingJob = it, pendingJob = null)
                        it.start()
                    }
                } else {
                    val (executingJob, pendingJob) = jobs
                    val newJob = launchLazyCoroutine(key, block)
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
            mutex.withLock {
                val jobs = jobMap[key]
                if (jobs == null) {
                    parentScope.launchLazyCoroutine(key, block).also {
                        jobMap[key] = Jobs(executingJob = it, pendingJob = null)
                        it.start()
                    }
                } else {
                    val (executingJob, pendingJob) = jobs
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
        mutex.withLock {
            val (executingJob, pendingJob) = jobMap[key] ?: return@withLock
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
            withContext(NonCancellable) {
                mutex.withLock {
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
}

/**
 * A class that holds the current executing job and the pending job for a given key.
 */
private data class Jobs(val executingJob: Job, val pendingJob: Job?)