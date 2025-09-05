package com.jsb.versachat.presentation.ui.state

import com.jsb.versachat.domain.model.ChatSession

data class ChatUiState(
    val sessions: List<ChatSession> = emptyList(),
    val currentSessionId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDrawerOpen: Boolean = false
) {
    val currentSession: ChatSession?
        get() = sessions.find { it.id == currentSessionId }

    val hasAnySessions: Boolean
        get() = sessions.isNotEmpty()
}