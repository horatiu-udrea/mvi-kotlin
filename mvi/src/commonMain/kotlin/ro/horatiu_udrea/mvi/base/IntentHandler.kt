package ro.horatiu_udrea.mvi.base

import ro.horatiu_udrea.mvi.operations.Operations
import kotlin.reflect.KClass

public fun interface IntentHandler<State, Intent : Any, Dependencies> {
    public suspend fun handle(
        intent: Intent,
        dependencies: Dependencies,
        operations: Operations<KClass<out Intent>>,
        changeState: suspend (StateChangeRequest<State, Intent>) -> Unit,
        sendIntent: (Intent) -> Unit
    )
}

public data class StateChangeRequest<State, Intent>(
    val description: String,
    val sourceIntent: Intent,
    val block: (State) -> State
)
