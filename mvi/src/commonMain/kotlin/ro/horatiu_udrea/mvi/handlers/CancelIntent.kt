package ro.horatiu_udrea.mvi.handlers

import ro.horatiu_udrea.mvi.base.IntentHandler
import ro.horatiu_udrea.mvi.base.StateChangeRequest
import ro.horatiu_udrea.mvi.operations.OperationScheduler
import kotlin.reflect.KClass

/**
 * Abstract class to cancel the handling of a specific intent.
 * Intent equality is determined based on its type, not its [equals] method.
 *
 * @param State The type representing the state of the component.
 * @param Intent The type representing the intents that can be sent to the component.
 * @param Dependencies The type representing the dependencies required by the component.
 * @property intentToCancel The class of the intent to be cancelled.
 */
public abstract class CancelIntent<State, Intent : IntentHandler<State, Intent, Dependencies>, Dependencies>(
    private val intentToCancel: KClass<Intent>
) : IntentHandler<State, Intent, Dependencies> {
    override suspend fun handle(
        intent: Intent,
        dependencies: Dependencies,
        scheduler: OperationScheduler<KClass<out Intent>>,
        changeState: suspend (StateChangeRequest<State, Intent>) -> Unit,
        sendIntent: (Intent) -> Unit
    ) {
        scheduler.cancel(intentToCancel)
        val intentName = intentToCancel.simpleName ?: "Anonymous intent"
        changeState(
            StateChangeRequest(
                "Cancelled intent $intentName",
                intent,
                block = { it }
            )
        )
    }
}