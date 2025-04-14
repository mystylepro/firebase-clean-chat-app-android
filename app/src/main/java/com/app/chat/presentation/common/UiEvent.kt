package com.app.chat.presentation.common

sealed class UiEvent {
    data object ShowLoader : UiEvent()
    data object HideLoader : UiEvent()
    data class ShowToast(val message: String) : UiEvent()
}
