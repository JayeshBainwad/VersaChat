package com.jsb.versachat.presentation.ui.state

import com.jsb.versachat.domain.model.ResponseStyle

sealed class ChatUiEvent {
    data class SendMessage(val content: String) : ChatUiEvent()
    data class CreateNewSession(val title: String) : ChatUiEvent()
    data class SwitchSession(val sessionId: String) : ChatUiEvent()
    data class UpdateResponseStyle(val sessionId: String, val responseStyle: ResponseStyle) : ChatUiEvent()
    data class RegenerateLastResponse(val responseStyle: ResponseStyle) : ChatUiEvent()
    data class DeleteSession(val sessionId: String) : ChatUiEvent()
    data class UpdateSessionTitle(val sessionId: String, val title: String) : ChatUiEvent()
    object ClearError : ChatUiEvent()
    object ToggleDrawer : ChatUiEvent()
}