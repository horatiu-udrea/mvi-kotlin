package ro.horatiu_udrea.mvi

import kotlinx.coroutines.flow.StateFlow

/**
 * An MVI-style component. Provides [state] that updates by handling intents sent with [sendIntent].
 */
public interface MVIComponent<out State, in Intent> {
    /**
     * State emitted from this component.
     */
    public val state: StateFlow<State>

    /**
     * Send an intent that will be handled by the component by updating the [state].
     */
    public fun sendIntent(intent: Intent)
}