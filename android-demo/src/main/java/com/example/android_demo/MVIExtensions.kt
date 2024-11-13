package com.example.android_demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ro.horatiu_udrea.mvi.MVIComponent

@Composable
fun <S, I> MVIComponent<S, I>.composableState() = state.collectAsState().value