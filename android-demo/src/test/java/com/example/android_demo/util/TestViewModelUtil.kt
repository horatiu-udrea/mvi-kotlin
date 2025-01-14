package com.example.android_demo.util

import com.example.android_demo.MVIViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import ro.horatiu_udrea.mvi.base.IntentHandler
import ro.horatiu_udrea.mvi.handlers.ComponentState
import ro.horatiu_udrea.mvi.handlers.SimpleIntentHandler

inline fun <S, I : IntentHandler<S, I, *>> TestScope.testViewModel(
    viewModel: MVIViewModel<S, I, *>,
    block: TestViewModelScope<S, I>.() -> Unit
) = TestViewModelScope(this, viewModel).block()

class TestViewModelScope<S, I : IntentHandler<S, I, *>>(private val testScope: TestScope, private val viewModel: MVIViewModel<S, I, *>) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: S
        get() {
            testScope.advanceUntilIdle()
            return viewModel.state.value
        }

    fun sendIntent(intent: I) = viewModel.sendIntent(intent)
}

suspend fun <S, I, T : SimpleIntentHandler<S, I, D>, D> testIntent(
    intent: T,
    dependencies: D,
    initialState: S
): IntentTestResult<S, I> {
    val currentState = MutableStateFlow(initialState)
    val producedStates = MutableStateFlow(emptyList<S>())
    val scheduledIntents = MutableStateFlow(emptyList<I>())

    val componentState = ComponentState(
        change = { _: String, block: (S) -> S ->
            val producedState = block(currentState.value)
            currentState.value = producedState
            producedStates.update { it + producedState }
        },
        schedule = { scheduledIntent: I -> scheduledIntents.update { it + scheduledIntent }}
    )

    with(intent) {
        dependencies.handle(componentState)
    }

    return IntentTestResult(producedStates.value, scheduledIntents.value)
}

data class IntentTestResult<S, I>(
    val producedStates: List<S>,
    val scheduledIntents: List<I>
)