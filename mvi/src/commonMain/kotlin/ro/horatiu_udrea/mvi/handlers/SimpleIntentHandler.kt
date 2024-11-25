package ro.horatiu_udrea.mvi.handlers

/**
 * Functional interface for handling simple intents in an MVI architecture.
 *
 * @param State The type representing the state of the component.
 * @param Intent The type representing the intents that can be sent to the component.
 * @param Dependencies The type representing the dependencies required by the component.
 */
public fun interface SimpleIntentHandler<State, Intent, Dependencies> {
    /**
     * Handles the intent.
     *
     * @param state The current state of the component.
     */
    public suspend fun Dependencies.handle(state: ComponentState<State, Intent>)
}

/**
 * Data class representing the state of a component.
 *
 * @param State The type representing the state of the component.
 * @param Intent The type representing the intents that can be sent to the component.
 * @param change Function to change the state.
 * @param schedule Function to schedule the handling of an intent.
 */
public data class ComponentState<State, Intent>(
    private val change: (description: String, block: (State) -> State) -> Unit,
    private val schedule: (Intent) -> Unit
) {
    /**
     * Changes the state with the given description and block.
     *
     * @param description Description of the state change.
     * @param block The function to change the state.
     */
    public fun change(description: String? = null, block: (State) -> State): Unit =
        change.invoke(description ?: "State changed with no description", block)

    /**
     * Reads the state with the given reason and block.
     *
     * @param reason Reason for reading the state.
     * @param block The function to read the state.
     * @return The result of the read operation.
     */
    public fun <R> read(
        reason: String? = null,
        block: (State) -> R
    ): R {
        var readState: Any? = NULL
        val description = reason?.let { "State read - $it" } ?: "State read with no specified reason"
        change(description) { oldState ->
            readState = block(oldState)
            oldState
        }
        if (readState === NULL) error("Change function did not run")

        @Suppress("UNCHECKED_CAST")
        return readState as R
    }

    /**
     * Keeps the current state for the given reason.
     *
     * @param reason Reason for keeping the same state.
     */
    public fun keep(reason: String) {
        change("State not changed - $reason") { it }
    }

    /**
     * Schedules an intent to be handled.
     *
     * @param intent The intent to be scheduled.
     */
    public fun schedule(intent: Intent): Unit = schedule.invoke(intent)

    private companion object {
        private val NULL = Any()
    }
}