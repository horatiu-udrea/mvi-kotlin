package ro.horatiu_udrea.mvi.handlers

public fun interface SimpleIntentHandler<State, Intent, Dependencies> {
    public suspend fun Dependencies.handle(state: ComponentState<State, Intent>)
}

public data class ComponentState<State, Intent>(
    val change: suspend (description: String, block: (State) -> State) -> Unit,
    val schedule: (Intent) -> Unit
)

private val NULL = Any()

private const val CHANGE_DEFAULT_DESCRIPTION = "Changed state with no description"
private const val READ_DEFAULT_DESCRIPTION = "Read state with no description"
private const val KEEP_DEFAULT_DESCRIPTION = "Kept state with no description"

public suspend fun <State, Intent> ComponentState<State, Intent>.change(block: (State) -> State) {
    change(CHANGE_DEFAULT_DESCRIPTION, block)
}

public suspend fun <State, Intent, R> ComponentState<State, Intent>.read(
    description: String = READ_DEFAULT_DESCRIPTION,
    block: (State) -> R
): R {
    var readState: Any? = NULL
    change(description) { oldState ->
        readState = block(oldState)
        oldState
    }
    if (readState === NULL) error("Change function did not run")

    @Suppress("UNCHECKED_CAST")
    return readState as R
}

public suspend fun <State, Intent> ComponentState<State, Intent>.keep(description: String = KEEP_DEFAULT_DESCRIPTION) {
    change(description) { it }
}
