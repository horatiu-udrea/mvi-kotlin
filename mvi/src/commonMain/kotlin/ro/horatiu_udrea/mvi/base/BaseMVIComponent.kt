package ro.horatiu_udrea.mvi.base

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ro.horatiu_udrea.mvi.MVIComponent
import ro.horatiu_udrea.mvi.operations.OperationsImpl
import kotlin.reflect.KClass

public abstract class BaseMVIComponent<State, Intent : IntentHandler<State, Intent, in Dependencies>, in Dependencies>(
    initialState: State,
    coroutineScope: CoroutineScope,
    mainDispatcher: CoroutineDispatcher,
    private val dependencies: Dependencies,
) : MVIComponent<State, Intent> {

    private val syncDispatcher = mainDispatcher.limitedParallelism(1)

    private val coroutineScope = CoroutineScope(
        SupervisorJob(parent = coroutineScope.coroutineContext[Job.Key]) +
                syncDispatcher +
                CoroutineName(this::class.simpleName ?: "BaseMVIComponent")
    )

    private val mutableState: MutableStateFlow<State> = MutableStateFlow(initialState)

    private val operations by lazy(LazyThreadSafetyMode.NONE) { OperationsImpl<KClass<out Intent>>(syncDispatcher) }

    override val state: StateFlow<State> = mutableState.asStateFlow()

    override fun sendIntent(intent: Intent) {
        onIntent(intent, ::sendIntent)
        coroutineScope.launch {
            try {
                intent.handle(
                    intent = intent,
                    dependencies = dependencies,
                    operations = operations,
                    changeState = { stateChangeHandler ->
                        withContext(syncDispatcher) {
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
                        }
                    },
                    sendIntent = ::sendIntent
                )
            } catch (_: CancellationException) {
                // ignore CancellationException
            } catch (e: Throwable) {
                onException(intent, e, ::sendIntent)
            }
        }
    }

    /**
     * Use this to cancel handling of all intents.
     * Cancellation will propagate to the provided [coroutineScope].
     */
    public fun cancelCoroutineScope(): Unit = coroutineScope.cancel()

    /**
     * Runs when an intent is received. Can be used to send other intents.
     */
    protected abstract fun onIntent(intent: Intent, sendIntent: (Intent) -> Unit)

    /**
     * Runs when the state is changed. Can be used to send other intents.
     */
    protected abstract fun onStateChange(
        description: String,
        sourceIntent: Intent,
        oldState: State,
        newState: State,
        sendIntent: (Intent) -> Unit
    )

    /**
     * Runs when an exception was not handled. Can be used to send other intents.
     */
    protected abstract fun onException(
        intent: Intent,
        exception: Throwable,
        sendIntent: (Intent) -> Unit
    )
}