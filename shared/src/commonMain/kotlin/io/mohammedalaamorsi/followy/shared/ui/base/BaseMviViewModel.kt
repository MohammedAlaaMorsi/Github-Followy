package io.mohammedalaamorsi.followy.shared.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Interface representing the state of the UI.
 */
interface UiState

/**
 * Interface representing a user action (Intent).
 */
interface UiIntent

/**
 * Interface representing a one-time side effect (e.g., navigation, snackbar).
 */
interface UiEffect

/**
 * Base ViewModel for MVI pattern.
 */
abstract class BaseMviViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    private val _intent = MutableSharedFlow<I>()

    init {
        viewModelScope.launch {
            _intent.collect { handleIntent(it) }
        }
    }

    /**
     * Submit an intent (user action).
     */
    fun sendIntent(intent: I) {
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }

    /**
     * Set a new state.
     */
    protected fun setState(newState: S) {
        _uiState.value = newState
    }

    /**
     * Set a side effect.
     */
    protected fun setEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * Process an intent.
     */
    protected abstract suspend fun handleIntent(intent: I)
}
