package com.jsb.versachat.presentaion.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsb.versachat.data.model.Message
import com.jsb.versachat.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository = ChatRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun sendMessage(userInput: String) {
        if (userInput.isBlank()) return

        // Add user message to UI
        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(Message("user", userInput))
        _uiState.value = _uiState.value.copy(messages = currentMessages, isLoading = true)

        viewModelScope.launch {
            // Get AI response
            val aiResponse = repository.getResponse(currentMessages)
            currentMessages.add(Message("assistant", aiResponse))
            _uiState.value = _uiState.value.copy(messages = currentMessages, isLoading = false)
        }
    }
}

data class UiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false
)