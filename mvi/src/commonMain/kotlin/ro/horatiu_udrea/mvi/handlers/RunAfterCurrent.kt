package ro.horatiu_udrea.mvi.handlers

import ro.horatiu_udrea.mvi.base.IntentHandler
import ro.horatiu_udrea.mvi.base.StateChangeRequest
import ro.horatiu_udrea.mvi.operations.OperationScheduler
import kotlin.reflect.KClass

/**
 * Abstract class for handling an intent after the current operation completes.
 * Intent equality is determined based on its type, not its [equals] method.
 *
 * @param State The type representing the state of the component.
 * @param Intent The type representing the intents that can be sent to the component.
 * @param Dependencies The type representing the dependencies required by the component.
 * @property block The simple intent handler block to be executed in parallel.
 */
public abstract class RunAfterCurrent<State, Intent : IntentHandler<State, Intent, Dependencies>, Dependencies>(
    private val block: SimpleIntentHandler<State, Intent, Dependencies>
) : IntentHandler<State, Intent, Dependencies>, SimpleIntentHandler<State, Intent, Dependencies> by block {
    override suspend fun handle(
        intent: Intent,
        dependencies: Dependencies,
        scheduler: OperationScheduler<KClass<out Intent>>,
        changeState: suspend (StateChangeRequest<State, Intent>) -> Unit,
        sendIntent: (Intent) -> Unit
    ) {
        scheduler.runAfterCurrent(intent::class) {
            dependencies.handle(
                ComponentState(
                    change = { description, block ->
                        changeState(StateChangeRequest(description, intent, block))
                    },
                    schedule = sendIntent
                )
            )
        }
    }
}