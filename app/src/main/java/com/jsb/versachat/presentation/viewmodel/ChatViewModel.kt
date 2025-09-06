package com.jsb.versachat.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsb.versachat.domain.model.ChatSession
import com.jsb.versachat.domain.model.Message
import com.jsb.versachat.domain.model.MessageRole
import com.jsb.versachat.domain.model.ResponseStyle
import com.jsb.versachat.domain.repository.ChatRepository
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
    private val sendMessageUseCase: SendMessageUseCase,
    private val chatRepository: ChatRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
        private const val DEFAULT_SESSION_TITLE = "General Chat"
    }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        initializeApp()
    }

    // Update your onEvent method to handle the new event:
    fun onEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.SendMessage -> sendMessage(event.content)
            is ChatUiEvent.CreateNewSession -> createNewSession(event.title)
            is ChatUiEvent.SwitchSession -> switchSession(event.sessionId)
            is ChatUiEvent.UpdateResponseStyle -> updateResponseStyle(event.sessionId, event.responseStyle)
            is ChatUiEvent.RegenerateLastResponse -> regenerateLastResponse(event.responseStyle)
            is ChatUiEvent.DeleteSession -> deleteSession(event.sessionId)
            is ChatUiEvent.UpdateSessionTitle -> updateSessionTitle(event.sessionId, event.title)
            is ChatUiEvent.ClearError -> clearError()
            is ChatUiEvent.ToggleDrawer -> toggleDrawer()
        }
    }

    private fun initializeApp() {
        Log.d(TAG, "Initializing app and loading sessions")

        viewModelScope.launch {
            // Collect sessions from database
            chatRepository.getAllSessions().collect { sessions ->
                Log.d(TAG, "Loaded ${sessions.size} sessions from database")

                if (sessions.isEmpty()) {
                    // Create default session if none exist
                    createDefaultSession()
                } else {
                    val currentSessionId = _uiState.value.currentSessionId
                        ?: sessions.firstOrNull()?.id

                    _uiState.value = _uiState.value.copy(
                        sessions = sessions,
                        currentSessionId = currentSessionId
                    )
                }
            }
        }
    }

    private suspend fun createDefaultSession() {
        Log.d(TAG, "Creating default session")

        val defaultSession = ChatSession(
            id = UUID.randomUUID().toString(),
            title = DEFAULT_SESSION_TITLE,
            responseStyle = ResponseStyle.DETAILED
        )

        when (val result = chatRepository.saveSession(defaultSession)) {
            is Result.Success -> {
                Log.d(TAG, "Default session created successfully")
            }
            is Result.Error -> {
                Log.e(TAG, "Failed to create default session", result.exception)
                showError("Failed to initialize chat: ${result.exception.toUserFriendlyMessage()}")
            }
            is Result.Loading -> {
                // Handle loading state if needed
            }
        }
    }

    // Add this to your ChatViewModel class:
    private fun regenerateLastResponse(responseStyle: ResponseStyle) {
        Log.d(TAG, "Regenerating last response with style: ${responseStyle.displayName}")

        val currentState = _uiState.value
        val currentSession = currentState.currentSession

        if (currentSession == null) {
            Log.e(TAG, "No current session found")
            showError("No active chat session")
            return
        }

        // Find the last AI message
        val lastAiMessage = currentSession.messages.lastOrNull { it.role == MessageRole.ASSISTANT }
        if (lastAiMessage == null) {
            showError("No AI response to regenerate")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)

            try {
                // Delete the last AI message from database
                when (val deleteResult = chatRepository.deleteLastAIMessage(currentSession.id)) {
                    is Result.Success -> {
                        Log.d(TAG, "Successfully deleted last AI message")

                        // Get the conversation history without the deleted AI message
                        val messagesWithoutLastAI = currentSession.messages.filter { it != lastAiMessage }

                        // Generate new AI response with the new style
                        when (val result = sendMessageUseCase(messagesWithoutLastAI, responseStyle)) {
                            is Result.Success -> {
                                Log.d(TAG, "Successfully generated new response")

                                // Save the new AI message
                                when (val saveResult = chatRepository.saveMessage(currentSession.id, result.data)) {
                                    is Result.Success -> {
                                        // Update session's response style preference
                                        val updatedSession = currentSession.copy(responseStyle = responseStyle)
                                        chatRepository.updateSession(updatedSession)

                                        Log.d(TAG, "AI message regenerated and saved successfully")
                                        _uiState.value = _uiState.value.copy(isLoading = false)
                                    }
                                    is Result.Error -> {
                                        Log.e(TAG, "Failed to save regenerated AI message", saveResult.exception)
                                        showError("Failed to save regenerated response: ${saveResult.exception.toUserFriendlyMessage()}")

                                        // Re-save the original message as fallback
                                        chatRepository.saveMessage(currentSession.id, lastAiMessage)
                                        _uiState.value = _uiState.value.copy(isLoading = false)
                                    }
                                    is Result.Loading -> {
                                        // Handle loading state
                                    }
                                }
                            }
                            is Result.Error -> {
                                Log.e(TAG, "Error regenerating AI response", result.exception)
                                showError("Failed to regenerate response: ${result.exception.toUserFriendlyMessage()}")

                                // Re-save the original message as fallback
                                chatRepository.saveMessage(currentSession.id, lastAiMessage)
                                _uiState.value = _uiState.value.copy(isLoading = false)
                            }
                            is Result.Loading -> {
                                // Handle loading state
                            }
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Failed to delete last AI message", deleteResult.exception)
                        showError("Failed to prepare for regeneration: ${deleteResult.exception.toUserFriendlyMessage()}")
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    is Result.Loading -> {
                        // Handle loading state
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during regeneration", e)
                showError("Unexpected error: ${e.toUserFriendlyMessage()}")
                _uiState.value = _uiState.value.copy(isLoading = false)
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

        viewModelScope.launch {
            val userMessage = Message(role = MessageRole.USER, content = content)

            // Save user message to database
            when (val saveResult = chatRepository.saveMessage(currentSession.id, userMessage)) {
                is Result.Success -> {
                    _uiState.value = currentState.copy(isLoading = true, error = null)

                    // Get AI response
                    val updatedMessages = currentSession.messages + userMessage
                    when (val result = sendMessageUseCase(updatedMessages, currentSession.responseStyle)) {
                        is Result.Success -> {
                            Log.d(TAG, "Received AI response")

                            // Save AI message to database
                            when (chatRepository.saveMessage(currentSession.id, result.data)) {
                                is Result.Success -> {
                                    Log.d(TAG, "AI message saved successfully")
                                    _uiState.value = _uiState.value.copy(isLoading = false)
                                }
                                is Result.Error -> {
                                    Log.e(TAG, "Failed to save AI message")
                                    showError("Failed to save AI response")
                                    _uiState.value = _uiState.value.copy(isLoading = false)
                                }
                                is Result.Loading -> {
                                    // Handle loading state
                                }
                            }
                        }
                        is Result.Error -> {
                            Log.e(TAG, "Error getting AI response", result.exception)
                            showError(result.exception.toUserFriendlyMessage())
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                        is Result.Loading -> {
                            // Handle loading state
                        }
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "Failed to save user message", saveResult.exception)
                    showError("Failed to send message: ${saveResult.exception.toUserFriendlyMessage()}")
                }
                is Result.Loading -> {
                    // Handle loading state
                }
            }
        }
    }

    private fun createNewSession(title: String) {
        Log.d(TAG, "Creating new session: $title")

        if (title.isBlank()) {
            showError("Session title cannot be empty")
            return
        }

        viewModelScope.launch {
            val newSession = ChatSession(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                responseStyle = ResponseStyle.DETAILED
            )

            when (val result = chatRepository.saveSession(newSession)) {
                is Result.Success -> {
                    Log.d(TAG, "New session created successfully: ${newSession.id}")
                    _uiState.value = _uiState.value.copy(
                        currentSessionId = newSession.id,
                        isDrawerOpen = false
                    )
                }
                is Result.Error -> {
                    Log.e(TAG, "Error creating new session", result.exception)
                    showError("Failed to create new session: ${result.exception.toUserFriendlyMessage()}")
                }
                is Result.Loading -> {
                    // Handle loading state
                }
            }
        }
    }

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

        viewModelScope.launch {
            val session = chatRepository.getSessionById(sessionId)
            if (session != null) {
                val updatedSession = session.copy(responseStyle = responseStyle)
                when (val result = chatRepository.updateSession(updatedSession)) {
                    is Result.Success -> {
                        Log.d(TAG, "Response style updated successfully")
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Error updating response style", result.exception)
                        showError("Failed to update response style: ${result.exception.toUserFriendlyMessage()}")
                    }
                    is Result.Loading -> {
                        // Handle loading state
                    }
                }
            } else {
                showError("Session not found")
            }
        }
    }

    private fun updateSessionTitle(sessionId: String, title: String) {
        Log.d(TAG, "Updating session title for $sessionId to: $title")

        if (title.isBlank()) {
            showError("Session title cannot be empty")
            return
        }

        viewModelScope.launch {
            val session = chatRepository.getSessionById(sessionId)
            if (session != null) {
                val updatedSession = session.copy(
                    title = title.trim(),
                    lastUpdated = System.currentTimeMillis()
                )
                when (val result = chatRepository.updateSession(updatedSession)) {
                    is Result.Success -> {
                        Log.d(TAG, "Session title updated successfully")
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Error updating session title", result.exception)
                        showError("Failed to update session title: ${result.exception.toUserFriendlyMessage()}")
                    }
                    is Result.Loading -> {
                        // Handle loading state
                    }
                }
            } else {
                showError("Session not found")
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

        viewModelScope.launch {
            when (val result = chatRepository.deleteSession(sessionId)) {
                is Result.Success -> {
                    Log.d(TAG, "Session deleted successfully")

                    // If we deleted the current session, switch to another one
                    if (currentState.currentSessionId == sessionId) {
                        val remainingSessions = currentState.sessions.filter { it.id != sessionId }
                        _uiState.value = currentState.copy(
                            currentSessionId = remainingSessions.firstOrNull()?.id
                        )
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "Error deleting session", result.exception)
                    showError("Failed to delete session: ${result.exception.toUserFriendlyMessage()}")
                }
                is Result.Loading -> {
                    // Handle loading state
                }
            }
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun toggleDrawer() {
        _uiState.value = _uiState.value.copy(isDrawerOpen = !_uiState.value.isDrawerOpen)
    }

    private fun showError(message: String) {
        Log.w(TAG, "Showing error: $message")
        _uiState.value = _uiState.value.copy(error = message)
    }
}