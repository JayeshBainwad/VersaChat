package com.jsb.versachat.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsb.versachat.domain.model.ChatSession
import com.jsb.versachat.domain.model.Message
import com.jsb.versachat.domain.model.MessageRole
import com.jsb.versachat.domain.model.ResponseStyle
import com.jsb.versachat.domain.usecase.SendMessageUseCase
import com.jsb.versachat.domain.util.Result
import com.jsb.versachat.domain.util.safeCall
import com.jsb.versachat.domain.util.toUserFriendlyMessage
import com.jsb.versachat.presentation.ui.state.ChatUiEvent
import com.jsb.versachat.presentation.ui.state.ChatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
        private const val DEFAULT_SESSION_TITLE = "General Chat"
    }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        initializeDefaultSession()
    }

    fun onEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.SendMessage -> sendMessage(event.content)
            is ChatUiEvent.CreateNewSession -> createNewSession(event.title)
            is ChatUiEvent.SwitchSession -> switchSession(event.sessionId)
            is ChatUiEvent.UpdateResponseStyle -> updateResponseStyle(event.sessionId, event.responseStyle)
            is ChatUiEvent.DeleteSession -> deleteSession(event.sessionId)
            is ChatUiEvent.ClearError -> clearError()
            is ChatUiEvent.ToggleDrawer -> toggleDrawer()
        }
    }

    private fun initializeDefaultSession() {
        Log.d(TAG, "Initializing default session")

        // Using safeCall utility for synchronous operations
        val result = safeCall(TAG) {
            val defaultSession = ChatSession(
                id = UUID.randomUUID().toString(),
                title = DEFAULT_SESSION_TITLE,
                responseStyle = ResponseStyle.DETAILED
            )

            _uiState.value = _uiState.value.copy(
                sessions = listOf(defaultSession),
                currentSessionId = defaultSession.id
            )

            Log.d(TAG, "Default session initialized successfully")
        }

        // Handle the result
        when (result) {
            is Result.Success -> {
                // Success handled in the action block
            }
            is Result.Error -> {
                Log.e(TAG, "Failed to initialize default session", result.exception)
                showError("Failed to initialize chat: ${result.exception.toUserFriendlyMessage()}")
            }
            is Result.Loading -> {
                // Not applicable for this case
            }
        }
    }

    private fun sendMessage(content: String) {
        Log.d(TAG, "Sending message: ${content.take(50)}...")

        if (content.isBlank()) {
            Log.w(TAG, "Attempted to send blank message")
            showError("Message cannot be empty")
            return
        }

        val currentState = _uiState.value
        val currentSession = currentState.currentSession

        if (currentSession == null) {
            Log.e(TAG, "No current session found")
            showError("No active chat session")
            return
        }

        // Using safeCall for preparing the message
        val preparationResult = safeCall(TAG) {
            val userMessage = Message(role = MessageRole.USER, content = content)
            val updatedMessages = currentSession.messages + userMessage
            val updatedSession = currentSession.copy(
                messages = updatedMessages,
                lastUpdated = System.currentTimeMillis()
            )

            updateSession(updatedSession)
            _uiState.value = currentState.copy(isLoading = true, error = null)

            Pair(updatedMessages, updatedSession)
        }

        when (preparationResult) {
            is Result.Success -> {
                val (updatedMessages, updatedSession) = preparationResult.data

                // Get AI response
                viewModelScope.launch {
                    when (val result = sendMessageUseCase(updatedMessages, currentSession.responseStyle)) {
                        is Result.Success -> {
                            Log.d(TAG, "Received AI response")
                            val finalMessages = updatedMessages + result.data
                            val finalSession = updatedSession.copy(
                                messages = finalMessages,
                                lastUpdated = System.currentTimeMillis()
                            )
                            updateSession(finalSession)
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                        is Result.Error -> {
                            Log.e(TAG, "Error getting AI response", result.exception)
                            showError(result.exception.toUserFriendlyMessage())
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                        is Result.Loading -> {
                            // Handle loading state if needed
                        }
                    }
                }
            }
            is Result.Error -> {
                Log.e(TAG, "Error preparing message", preparationResult.exception)
                showError("Failed to send message: ${preparationResult.exception.toUserFriendlyMessage()}")
            }
            is Result.Loading -> {
                // Not applicable for this case
            }
        }
    }

    private fun createNewSession(title: String) {
        Log.d(TAG, "Creating new session: $title")

        if (title.isBlank()) {
            showError("Session title cannot be empty")
            return
        }

        // Using safeCall utility
        val result = safeCall(TAG) {
            val newSession = ChatSession(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                responseStyle = ResponseStyle.DETAILED
            )

            val currentState = _uiState.value
            val updatedSessions = currentState.sessions + newSession

            _uiState.value = currentState.copy(
                sessions = updatedSessions,
                currentSessionId = newSession.id,
                isDrawerOpen = false
            )

            Log.d(TAG, "New session created successfully: ${newSession.id}")
        }

        when (result) {
            is Result.Success -> {
                // Success handled in action block
            }
            is Result.Error -> {
                Log.e(TAG, "Error creating new session", result.exception)
                showError("Failed to create new session: ${result.exception.toUserFriendlyMessage()}")
            }
            is Result.Loading -> {
                // Not applicable
            }
        }
    }

    // Rest of the methods remain the same...
    private fun switchSession(sessionId: String) {
        Log.d(TAG, "Switching to session: $sessionId")

        val currentState = _uiState.value
        val sessionExists = currentState.sessions.any { it.id == sessionId }

        if (!sessionExists) {
            Log.w(TAG, "Attempted to switch to non-existent session: $sessionId")
            showError("Session not found")
            return
        }

        _uiState.value = currentState.copy(
            currentSessionId = sessionId,
            isDrawerOpen = false
        )
        Log.d(TAG, "Switched to session successfully")
    }

    private fun updateResponseStyle(sessionId: String, responseStyle: ResponseStyle) {
        Log.d(TAG, "Updating response style for session $sessionId to ${responseStyle.displayName}")

        val result = safeCall(TAG) {
            val currentState = _uiState.value
            val updatedSessions = currentState.sessions.map { session ->
                if (session.id == sessionId) {
                    session.copy(responseStyle = responseStyle)
                } else {
                    session
                }
            }

            _uiState.value = currentState.copy(sessions = updatedSessions)
            Log.d(TAG, "Response style updated successfully")
        }

        when (result) {
            is Result.Error -> {
                Log.e(TAG, "Error updating response style", result.exception)
                showError("Failed to update response style: ${result.exception.toUserFriendlyMessage()}")
            }
            else -> {
                // Success or other states
            }
        }
    }

    private fun deleteSession(sessionId: String) {
        Log.d(TAG, "Deleting session: $sessionId")

        val currentState = _uiState.value
        if (currentState.sessions.size <= 1) {
            showError("Cannot delete the last session")
            return
        }

        val result = safeCall(TAG) {
            val updatedSessions = currentState.sessions.filter { it.id != sessionId }
            val newCurrentId = if (currentState.currentSessionId == sessionId) {
                updatedSessions.firstOrNull()?.id
            } else {
                currentState.currentSessionId
            }

            _uiState.value = currentState.copy(
                sessions = updatedSessions,
                currentSessionId = newCurrentId
            )

            Log.d(TAG, "Session deleted successfully")
        }

        when (result) {
            is Result.Error -> {
                Log.e(TAG, "Error deleting session", result.exception)
                showError("Failed to delete session: ${result.exception.toUserFriendlyMessage()}")
            }
            else -> {
                // Success
            }
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun toggleDrawer() {
        _uiState.value = _uiState.value.copy(isDrawerOpen = !_uiState.value.isDrawerOpen)
    }

    private fun updateSession(updatedSession: ChatSession) {
        val currentState = _uiState.value
        val updatedSessions = currentState.sessions.map { session ->
            if (session.id == updatedSession.id) updatedSession else session
        }
        _uiState.value = currentState.copy(sessions = updatedSessions)
    }

    private fun showError(message: String) {
        Log.w(TAG, "Showing error: $message")
        _uiState.value = _uiState.value.copy(error = message)
    }
}