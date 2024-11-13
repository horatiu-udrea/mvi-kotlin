package ro.horatiu_udrea.mvi.scheduler

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import ro.horatiu_udrea.mvi.operations.OperationSchedulerImpl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Suppress("unused")
class OperationSchedulerImplTest : FeatureSpec({
    coroutineTestScope = true
    feature("runIfNotRunning") {
        scenario("should run when no other operation for the same key is already running") {
            val key1 = Any()
            val key2 = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))

            val operation1 = Operation(1, 1000.milliseconds)
            launch {
                scheduler.runIfNotRunning(key1, operation1::run)
            }

            val operation2 = Operation(2, 0.milliseconds)
            launch {
                delay(500.milliseconds)
                scheduler.runIfNotRunning(key2, operation2::run)
            }

            with(testCoroutineScheduler) {
                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation1 shouldNot beCompleted()
                operation2 should beCompleted()

                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation1 should beCompleted()
            }
        }
        scenario("should not run when another operation is already running for the same key") {
            val key = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))
            val operation1 = Operation(1, 1000.milliseconds)
            launch {
                scheduler.runIfNotRunning(key, operation1::run)
            }
            val operation2 = Operation(2, 0.milliseconds)
            launch {
                delay(500.milliseconds)
                scheduler.runIfNotRunning(key, operation2::run)
            }
            with(testCoroutineScheduler) {
                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation1 shouldNot beCompleted()
                operation2 shouldNot beCompleted()

                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation1 should beCompleted()
                operation2 shouldNot beStarted()
            }
        }
        scenario("should be cancellable") {
            val key = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))

            val operation = Operation(1, 1000.milliseconds)
            launch {
                scheduler.runIfNotRunning(key, operation::run)
            }

            launch {
                delay(100.milliseconds)
                scheduler.cancel(key)
            }

            with(testCoroutineScheduler) {
                runCurrent()
                operation should beStarted()

                advanceUntilIdle()
                operation shouldNot beCompleted()
            }
        }
    }
    feature("runAfterCurrent") {
        scenario("should run when no other operation for the same key is already running") {
            val key1 = Any()
            val key2 = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))

            val operation1 = Operation(1, 1000.milliseconds)
            launch {
                scheduler.runAfterCurrent(key1, operation1::run)
            }

            val operation2 = Operation(2, 0.milliseconds)
            launch {
                delay(500.milliseconds)
                scheduler.runAfterCurrent(key2, operation2::run)
            }

            with(testCoroutineScheduler) {
                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation1 shouldNot beCompleted()
                operation2 should beCompleted()

                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation1 should beCompleted()
            }
        }
        scenario("should run after already running operation for the same key") {
            val key = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))

            val operation1 = Operation(1, 1000.milliseconds)
            launch {
                scheduler.runAfterCurrent(key, operation1::run)
            }

            val operation2 = Operation(2, 500.milliseconds)
            launch {
                delay(500.milliseconds)
                scheduler.runAfterCurrent(key, operation2::run)
            }

            with(testCoroutineScheduler) {
                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation1 shouldNot beCompleted()
                operation2 shouldNot beStarted()

                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation1 should beCompleted()
                operation2 shouldNot beCompleted()

                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation2 should beCompleted()
            }
        }
        scenario("should replace scheduled operation for the same key") {
            val key = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))

            val operation1 = Operation(1, 1000.milliseconds)
            launch {
                scheduler.runAfterCurrent(key, operation1::run)
            }

            val operation2 = Operation(2, 500.milliseconds)
            launch {
                delay(100.milliseconds)
                scheduler.runAfterCurrent(key, operation2::run)
            }

            val operation3 = Operation(3, 500.milliseconds)
            launch {
                delay(200.milliseconds)
                scheduler.runAfterCurrent(key, operation3::run)
            }

            with(testCoroutineScheduler) {
                advanceTimeBy(1000.milliseconds)
                runCurrent()
                operation1 should beCompleted()
                operation2 shouldNot beCompleted()
                operation3 shouldNot beCompleted()

                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation2 shouldNot beCompleted()
                operation3 should beCompleted()

                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation2 shouldNot beCompleted()
            }
        }
        scenario("should be cancellable") {
            val key = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))

            val operation = Operation(1, 1000.milliseconds)
            launch {
                scheduler.runAfterCurrent(key, operation::run)
            }

            launch {
                delay(100.milliseconds)
                scheduler.cancel(key)
            }

            with(testCoroutineScheduler) {
                runCurrent()
                operation should beStarted()

                advanceUntilIdle()
                operation shouldNot beCompleted()
            }
        }
    }
    feature("cancelCurrentAndRun") {
        scenario("should run when no other operation for the same key is already running") {
            val key1 = Any()
            val key2 = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))

            val operation1 = Operation(1, 1000.milliseconds)
            launch {
                scheduler.cancelCurrentThenRun(key1, operation1::run)
            }

            val operation2 = Operation(2, 0.milliseconds)
            launch {
                delay(500.milliseconds)
                scheduler.cancelCurrentThenRun(key2, operation2::run)
            }

            with(testCoroutineScheduler) {
                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation1 shouldNot beCompleted()
                operation2 should beCompleted()

                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation1 should beCompleted()
            }
        }
        scenario("should run and cancel current operation for the same key that is already running") {
            val key = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))

            val operation1 = Operation(1, 1000.milliseconds)
            launch {
                scheduler.cancelCurrentThenRun(key, operation1::run)
            }

            val operation2 = Operation(2, 500.milliseconds)
            launch {
                delay(500.milliseconds)
                scheduler.cancelCurrentThenRun(key, operation2::run)
            }

            with(testCoroutineScheduler) {
                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation1 shouldNot beCompleted()
                operation2 shouldNot beCompleted()

                advanceTimeBy(500.milliseconds)
                runCurrent()
                operation1 shouldNot beCompleted()
                operation2 should beCompleted()

                advanceUntilIdle()
                operation1 shouldNot beCompleted()
            }
        }
        scenario("should be cancellable") {
            val key = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))

            val operation = Operation(1, 1000.milliseconds)
            launch {
                scheduler.cancelCurrentThenRun(key, operation::run)
            }

            launch {
                delay(100.milliseconds)
                scheduler.cancel(key)
            }

            with(testCoroutineScheduler) {
                runCurrent()
                operation should beStarted()

                advanceUntilIdle()
                operation shouldNot beCompleted()
            }
        }
    }
    feature("operations should work when mixed") {
        scenario("run operation, schedule another, cancel and run a new one, without the previous ones completing") {
            val key = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))
            val operation1 = Operation(1, 1000.milliseconds)
            launch {
                scheduler.runIfNotRunning(key, operation1::run)
            }

            val operation2 = Operation(2, 1000.milliseconds)
            launch {
                delay(100.milliseconds)
                scheduler.runAfterCurrent(key, operation2::run)
            }

            val operation3 = Operation(3, 500.milliseconds)
            launch {
                delay(200.milliseconds)
                scheduler.cancelCurrentThenRun(key, operation3::run)
            }

            with(testCoroutineScheduler) {
                advanceUntilIdle()
                operation1 should beStarted()
                operation1 shouldNot beCompleted()
                operation2 shouldNot beStarted()
                operation3 should beCompleted()
            }
        }
        scenario("run operation, schedule another, cancel and run a new one, after the second one completes") {
            val key = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))
            val operation1 = Operation(1, 1000.milliseconds)
            launch {
                scheduler.runIfNotRunning(key, operation1::run)
            }

            val operation2 = Operation(2, 500.milliseconds)
            launch {
                delay(100.milliseconds)
                scheduler.runAfterCurrent(key, operation2::run)
            }

            val operation3 = Operation(3, 500.milliseconds)
            launch {
                delay(1200.milliseconds)
                scheduler.cancelCurrentThenRun(key, operation3::run)
            }

            with(testCoroutineScheduler) {
                advanceTimeBy(1000.milliseconds)
                runCurrent()
                operation1 should beCompleted()
                operation2 should beStarted()
                operation3 shouldNot beCompleted()

                advanceUntilIdle()
                operation2 shouldNot beCompleted()
                operation3 should beCompleted()
            }
        }
        scenario("run an operation an schedule another, then cancel everything") {
            val key = Any()
            val scheduler = OperationSchedulerImpl<Any>(StandardTestDispatcher(testCoroutineScheduler))
            val operation1 = Operation(1, 1000.milliseconds)
            launch {
                scheduler.runIfNotRunning(key, operation1::run)
            }

            val operation2 = Operation(2, 500.milliseconds)
            launch {
                delay(100.milliseconds)
                scheduler.runAfterCurrent(key, operation2::run)
            }

            launch {
                delay(200.milliseconds)
                scheduler.cancel(key)
            }

            with(testCoroutineScheduler) {
                advanceTimeBy(100.milliseconds)
                runCurrent()
                operation1 should beStarted()
                operation2 shouldNot beStarted()

                advanceUntilIdle()
                operation1 shouldNot beCompleted()
                operation2 shouldNot beStarted()
            }
        }
    }
})

class Operation(val number: Int, private val duration: Duration) {
    var started = false
    var completed = false

    suspend fun run() {
        started = true
        delay(duration)
        completed = true
    }
}

fun beCompleted(): Matcher<Operation> = object : Matcher<Operation> {
    override fun test(value: Operation) = MatcherResult(
        value.completed,
        { "Operation ${value.number} should be completed" },
        { "Operation ${value.number} should not be completed" }
    )
}

fun beStarted(): Matcher<Operation> = object : Matcher<Operation> {
    override fun test(value: Operation) = MatcherResult(
        value.started,
        { "Operation ${value.number} should be started" },
        { "Operation ${value.number} should not be started" }
    )
}
