package com.jsb.versachat.domain.usecase

import com.jsb.versachat.domain.model.Message
import com.jsb.versachat.domain.model.MessageRole
import com.jsb.versachat.domain.model.ResponseStyle
import com.jsb.versachat.domain.repository.ChatRepository
import com.jsb.versachat.domain.util.Result
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(
        messages: List<Message>,
        responseStyle: ResponseStyle
    ): Result<Message> {
        return when (val result = repository.getChatResponse(messages, responseStyle)) {
            is Result.Success -> {
                val assistantMessage = Message(
                    role = MessageRole.ASSISTANT,
                    content = result.data
                )
                Result.Success(assistantMessage)
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }
}