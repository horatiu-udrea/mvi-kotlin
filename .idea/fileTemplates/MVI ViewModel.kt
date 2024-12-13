#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
#parse("File Header.java")
import ro.horatiu_udrea.mvi.base.IntentHandler
import ro.horatiu_udrea.mvi.handlers.Run
import ro.horatiu_udrea.mvi.handlers.RunIfNotRunning

internal typealias S = ${VIEWMODEL_NAME}State
internal typealias I = ${VIEWMODEL_NAME}Intent
internal typealias D = ${VIEWMODEL_NAME}Dependencies

class ${VIEWMODEL_NAME}ViewModel(dependencies: D) : MVIViewModel<S, I, D>(initialState = ${VIEWMODEL_NAME}State(), dependencies) {

    override fun onIntent(
        intent: I,
        sendIntent: (I) -> Unit
    ) = Unit

    override fun onStateChange(
        description: String,
        sourceIntent: I,
        oldState: S,
        newState: S,
        sendIntent: (I) -> Unit
    ) = Unit

    override fun onException(
        intent: I,
        exception: Throwable,
        sendIntent: (I) -> Unit
    ) {
        super.onException(intent, exception, sendIntent)
    }
}

data class ${VIEWMODEL_NAME}State(
    val state: Unit = Unit
)

sealed interface ${VIEWMODEL_NAME}Intent : IntentHandler<S, I, D> {
    data object Initialize : I, RunIfNotRunning<S, I, D>({ state ->
        state.change("Description") { oldState -> oldState.copy() }
    })
}

class ${VIEWMODEL_NAME}Dependencies(
    
)