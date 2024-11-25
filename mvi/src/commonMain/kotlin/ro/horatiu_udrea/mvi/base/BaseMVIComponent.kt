package ro.horatiu_udrea.mvi.base

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ro.horatiu_udrea.mvi.MVIComponent
import ro.horatiu_udrea.mvi.operations.OperationSchedulerImpl
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

/**
 * Base class for Model-View-Intent (MVI) components.
 *
 * @param State The type representing the state of the component.
 * @param Intent The type representing the intents that can be sent to the component.
 * @param Dependencies The type representing the dependencies required by the component.
 * @param initialState The initial state of the component.
 * @param dependencies The dependencies required by the component.
 * @param coroutineContext The coroutine context in which the component operates.
 */
public abstract class BaseMVIComponent<State, Intent : IntentHandler<State, Intent, in Dependencies>, in Dependencies>(
    initialState: State,
    private val dependencies: Dependencies,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
) : MVIComponent<State, Intent> {

    /**
     * Coroutine scope for managing coroutines within the component.
     */
    private val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob(parent = coroutineContext[Job]))

    /**
     * Mutable state flow to hold the current state.
     */
    private val mutableState: MutableStateFlow<State> = MutableStateFlow(initialState)

    /**
     * Used for scheduling intent handlers by key.
     */
    private val scheduler = OperationSchedulerImpl<KClass<out Intent>>()

    /**
     * State emitted from this component.
     */
    override val state: StateFlow<State> = mutableState.asStateFlow()

    /**
     * Send an [intent] that will be handled by the component by updating the [state].
     */
    override fun sendIntent(intent: Intent) {
        onIntent(intent, ::sendIntent)
        coroutineScope.launch {
            try {
                intent.handle(
                    intent = intent,
                    dependencies = dependencies,
                    scheduler = scheduler,
                    changeState = { stateChangeHandler ->
                        mutableState.update { oldState ->
                            val newState = stateChangeHandler.block(oldState)
                            onStateChange(
                                stateChangeHandler.description,
                                intent,
                                oldState,
                                newState,
                                ::sendIntent
                            )
                            return@update newState
                        }
                    },
                    sendIntent = ::sendIntent
                )
            } catch (e: CancellationException) {
                // Propagate CancellationException
                throw e
            } catch (e: Throwable) {
                onException(intent, e, ::sendIntent)
            }
        }
    }

    /**
     * Cancel handling of all intents.
     * Cancellation will propagate to job in the coroutineContext, if there is any.
     */
    public fun cancelCoroutineScope(): Unit = coroutineScope.cancel()

    /**
     * Called when an intent is received. Can be used to send other intents.
     *
     * @param intent The received intent.
     * @param sendIntent Function to send another intent.
     */
    protected abstract fun onIntent(intent: Intent, sendIntent: (Intent) -> Unit)

    /**
     * Called when the state is changed. Can be used to send other intents.
     *
     * @param description Description of the state change.
     * @param sourceIntent The intent that caused the state change.
     * @param oldState The previous state.
     * @param newState The new state.
     * @param sendIntent Function to send another intent.
     */
    protected abstract fun onStateChange(
        description: String,
        sourceIntent: Intent,
        oldState: State,
        newState: State,
        sendIntent: (Intent) -> Unit
    )

    /**
     * Called when an exception is not handled. Can be used to send other intents.
     *
     * @param intent The intent that caused the exception.
     * @param exception The unhandled exception.
     * @param sendIntent Function to send another intent.
     */
    protected abstract fun onException(
        intent: Intent,
        exception: Throwable,
        sendIntent: (Intent) -> Unit
    )
}