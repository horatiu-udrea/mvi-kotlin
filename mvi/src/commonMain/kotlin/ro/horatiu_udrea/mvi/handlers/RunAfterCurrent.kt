package ro.horatiu_udrea.mvi.handlers

import ro.horatiu_udrea.mvi.base.IntentHandler
import ro.horatiu_udrea.mvi.base.StateChangeRequest
import ro.horatiu_udrea.mvi.operations.Operations
import kotlin.reflect.KClass

public abstract class RunAfterCurrent<State, Intent : IntentHandler<State, Intent, Dependencies>, Dependencies>(
    private val block: SimpleIntentHandler<State, Intent, Dependencies>
) : IntentHandler<State, Intent, Dependencies>, SimpleIntentHandler<State, Intent, Dependencies> by block {
    override suspend fun handle(
        intent: Intent,
        dependencies: Dependencies,
        operations: Operations<KClass<out Intent>>,
        changeState: suspend (StateChangeRequest<State, Intent>) -> Unit,
        sendIntent: (Intent) -> Unit
    ) {
        operations.runAfterCurrent(intent::class) {
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