package ro.horatiu_udrea.mvi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import ro.horatiu_udrea.mvi.MVIComponent
import ro.horatiu_udrea.mvi.base.BaseMVIComponent
import ro.horatiu_udrea.mvi.base.IntentHandler

abstract class MVIViewModel<State, Intent : IntentHandler<State, Intent, Dependencies>, Dependencies>(
    initialState: State,
    dependencies: Dependencies,
) : ViewModel(), MVIComponent<State, Intent> {

    // We need to create the mviComponent inside the constructor because otherwise we cannot get access to viewModelScope
    private val mviComponent: MVIComponent<State, Intent> =
        object : BaseMVIComponent<State, Intent, Dependencies>(
            coroutineScope = viewModelScope,
            mainDispatcher = Dispatchers.Main.immediate,
            initialState = initialState,
            dependencies = dependencies,
        ) {
            override fun onIntent(intent: Intent, sendIntent: (Intent) -> Unit) =
                this@MVIViewModel.onIntent(intent, sendIntent)

            override fun onStateChange(
                description: String,
                sourceIntent: Intent,
                oldState: State,
                newState: State,
                sendIntent: (Intent) -> Unit
            ) = this@MVIViewModel.onStateChange(description, sourceIntent, oldState, newState, ::sendIntent)

            override fun onException(
                intent: Intent,
                exception: Throwable,
                sendIntent: (Intent) -> Unit
            ) = this@MVIViewModel.onException(intent, exception, sendIntent)
        }

    override val state: StateFlow<State> = mviComponent.state

    override fun sendIntent(intent: Intent) = mviComponent.sendIntent(intent)

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
    protected open fun onException(
        intent: Intent,
        exception: Throwable,
        sendIntent: (Intent) -> Unit
    ) {
        if (debugMode) throw exception // Crash app in debug mode to find bugs easier in tests (manual + automated)
        else Log.e(
            this::class.simpleName,
            "Unhandled exception when handling intent ${intent::class.simpleName}",
            exception
        )
    }
}

private const val debugMode = true // Change this depending on build type