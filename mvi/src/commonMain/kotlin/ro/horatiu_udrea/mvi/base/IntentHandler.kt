package ro.horatiu_udrea.mvi.base

import ro.horatiu_udrea.mvi.operations.OperationScheduler
import kotlin.reflect.KClass

/**
 * Functional interface for handling intents in an MVI architecture.
 *
 * @param State The type representing the state of the component.
 * @param Intent The type representing the intents that can be sent to the component.
 * @param Dependencies The type representing the dependencies required by the component.
 */
public fun interface IntentHandler<State, Intent : Any, Dependencies> {
    /**
     * Handles an intent.
     *
     * @param intent The intent to be handled.
     * @param dependencies The dependencies required by the component.
     * @param scheduler The component used to schedule execution.
     * @param changeState Function to change the state.
     * @param sendIntent Function to send another intent.
     */
    public suspend fun handle(
        intent: Intent,
        dependencies: Dependencies,
        scheduler: OperationScheduler<KClass<out Intent>>,
        changeState: suspend (StateChangeRequest<State, Intent>) -> Unit,
        sendIntent: (Intent) -> Unit
    )
}

/**
 * Data class representing a request to change the state.
 *
 * @param State The type representing the state of the component.
 * @param Intent The type representing the intents that can be sent to the component.
 * @property description Description of the state change.
 * @property sourceIntent The intent that caused the state change.
 * @property block The function to change the state.
 */
public data class StateChangeRequest<State, Intent>(
    val description: String,
    val sourceIntent: Intent,
    val block: (State) -> State
)