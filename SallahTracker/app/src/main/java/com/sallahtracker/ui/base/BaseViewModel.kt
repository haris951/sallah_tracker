package com.sallahtracker.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel(), MviViewModel<S, I, E> {

    private val _uiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<E>()
    override val uiEffect: SharedFlow<E> = _uiEffect.asSharedFlow()

    protected fun setState(reduce: S.() -> S) {
        _uiState.value = _uiState.value.reduce()
    }

    protected fun setEffect(effect: E) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}