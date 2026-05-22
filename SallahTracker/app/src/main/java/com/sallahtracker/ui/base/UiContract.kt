package com.sallahtracker.ui.base

interface UiState
interface UiIntent
interface UiEffect

interface MviViewModel<S : UiState, I : UiIntent, E : UiEffect> {
    val uiState: kotlinx.coroutines.flow.StateFlow<S>
    val uiEffect: kotlinx.coroutines.flow.SharedFlow<E>
    fun onIntent(intent: I)
}