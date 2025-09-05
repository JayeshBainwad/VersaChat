package com.jsb.versachat.domain.repository

import com.jsb.versachat.domain.model.Message
import com.jsb.versachat.domain.model.ResponseStyle
import com.jsb.versachat.domain.util.Result

interface ChatRepository {
    suspend fun getChatResponse(
        messages: List<Message>,
        responseStyle: ResponseStyle
    ): Result<String>
}