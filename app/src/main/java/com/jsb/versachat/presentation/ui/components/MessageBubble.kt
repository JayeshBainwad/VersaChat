package com.jsb.versachat.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jsb.versachat.domain.model.Message
import com.jsb.versachat.domain.model.MessageRole
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MessageBubble(message: Message) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.role == MessageRole.USER)
            Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (message.role == MessageRole.USER)
                        MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            if (message.role == MessageRole.USER) {
                // User messages - plain text
                MarkdownText(
                    markdown = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
//                Text(
//                    text = message.content,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onPrimaryContainer
//                )
            } else {
                // AI messages - render markdown
                MarkdownText(
                    markdown = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}