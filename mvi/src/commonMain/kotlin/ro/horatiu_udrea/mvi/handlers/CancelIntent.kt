package ro.horatiu_udrea.mvi.handlers

import ro.horatiu_udrea.mvi.base.IntentHandler
import ro.horatiu_udrea.mvi.base.StateChangeRequest
import ro.horatiu_udrea.mvi.operations.Operations
import kotlin.reflect.KClass

public abstract class CancelIntent<State, Intent : IntentHandler<State, Intent, Dependencies>, Dependencies>(
    private val intentToCancel: KClass<Intent>
) : IntentHandler<State, Intent, Dependencies> {
    override suspend fun handle(
        intent: Intent,
        dependencies: Dependencies,
        operations: Operations<KClass<out Intent>>,
        changeState: suspend (StateChangeRequest<State, Intent>) -> Unit,
        sendIntent: (Intent) -> Unit
    ) {
        operations.cancel(intentToCancel)
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