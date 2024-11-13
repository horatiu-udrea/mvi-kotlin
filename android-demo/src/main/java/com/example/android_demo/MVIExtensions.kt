package com.example.android_demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ro.horatiu_udrea.mvi.MVIComponent

/**
 * Extension function for MVIComponent to collect the state as a Composable.
 *
 * @return The current state value.
 */
@Composable
fun <S, I> MVIComponent<S, I>.composableState() = state.collectAsState().value

/**
 * Composable function to handle side effects based on a Boolean trigger.
 *
 * @param trigger A Boolean value to trigger the effect.
 * @param effect A suspend function to be executed when the trigger is true.
 * @param dismissTrigger A function to dismiss the trigger after the effect is executed.
 */
@Composable
fun Effect(trigger: Boolean, effect: suspend () -> Unit, dismissTrigger: () -> Unit) {
    if(trigger) {
        LaunchedEffect(Unit) {
            effect()
            dismissTrigger()
        }
    }
}

/**
 * Composable function to handle side effects based on a generic nullable trigger.
 *
 * @param trigger A generic value to trigger the effect.
 * @param effect A suspend function to be executed when the trigger is not null.
 * @param dismissTrigger A function to dismiss the trigger after the effect is executed.
 */
@Composable
fun <T> Effect(trigger: T, effect: suspend (T & Any) -> Unit, dismissTrigger: (T & Any) -> Unit) {
    if(trigger != null) {
        LaunchedEffect(trigger) {
            effect(trigger)
            dismissTrigger(trigger)
        }
    }
}