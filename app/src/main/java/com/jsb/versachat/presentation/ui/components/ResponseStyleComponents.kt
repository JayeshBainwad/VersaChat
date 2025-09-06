package com.jsb.versachat.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jsb.versachat.domain.model.Message
import com.jsb.versachat.domain.model.MessageRole
import com.jsb.versachat.domain.model.ResponseStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponseStyleSelector(
    currentStyle: ResponseStyle,
    onStyleChanged: (ResponseStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Response Style",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResponseStyle.entries.forEach { style ->
                    ResponseStyleChip(
                        style = style,
                        isSelected = style == currentStyle,
                        onSelected = { onStyleChanged(style) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponseStyleChip(
    style: ResponseStyle,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = {
            Text(
                text = style.displayName,
                style = MaterialTheme.typography.bodySmall
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun MessageWithActions(
    message: Message,
    currentResponseStyle: ResponseStyle,
    onRegenerateResponse: (ResponseStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Original message bubble
        MessageBubble(message = message)

        // Action buttons for AI messages only
        if (message.role == MessageRole.ASSISTANT) {
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                var showStyleDialog by remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = { showStyleDialog = true },
                    modifier = Modifier.padding(end = 8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Regenerate",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (showStyleDialog) {
                    RegenerateStyleDialog(
                        currentStyle = currentResponseStyle,
                        onStyleSelected = { newStyle ->
                            onRegenerateResponse(newStyle)
                            showStyleDialog = false
                        },
                        onDismiss = { showStyleDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
fun RegenerateStyleDialog(
    currentStyle: ResponseStyle,
    onStyleSelected: (ResponseStyle) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStyle by remember { mutableStateOf(currentStyle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Regenerate with different style") },
        text = {
            Column {
                Text(
                    text = "Choose how detailed you want the response to be:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ResponseStyle.entries.forEach { style ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedStyle = style },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStyle == style,
                            onClick = { selectedStyle = style }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = style.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = getStyleDescription(style),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onStyleSelected(selectedStyle)
                    }
                ) {
                    Text("Save")
                }
            }
        }
    )
}

private fun getStyleDescription(style: ResponseStyle): String {
    return when (style) {
        ResponseStyle.SHORT -> "Quick, concise answers (1-2 sentences)"
        ResponseStyle.DETAILED -> "Balanced responses with context (2-4 paragraphs)"
        ResponseStyle.EXPLANATORY -> "In-depth explanations with examples (3-5 paragraphs)"
    }
}