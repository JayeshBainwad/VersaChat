package com.jsb.versachat.presentation.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jsb.versachat.domain.model.ChatSession
import com.jsb.versachat.domain.model.ResponseStyle
import com.jsb.versachat.presentation.ui.state.ChatUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    sessions: List<ChatSession>,
    currentSessionId: String?,
    onEvent: (ChatUiEvent) -> Unit
) {
    var newSessionTitle by remember { mutableStateOf("") }
    var showNewSessionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chat Sessions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showNewSessionDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Session")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sessions list
        LazyColumn {
            items(sessions, key = { it.id }) { session ->
                SessionItem(
                    session = session,
                    isSelected = session.id == currentSessionId,
                    onSessionClick = { onEvent(ChatUiEvent.SwitchSession(session.id)) },
                    onDeleteClick = { onEvent(ChatUiEvent.DeleteSession(session.id)) },
                    onStyleChange = { style ->
                        onEvent(ChatUiEvent.UpdateResponseStyle(session.id, style))
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // New session dialog
    if (showNewSessionDialog) {
        AlertDialog(
            onDismissRequest = {
                showNewSessionDialog = false
                newSessionTitle = ""
            },
            title = { Text("New Chat Session") },
            text = {
                OutlinedTextField(
                    value = newSessionTitle,
                    onValueChange = { newSessionTitle = it },
                    placeholder = { Text("Enter session name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSessionTitle.isNotBlank()) {
                            onEvent(ChatUiEvent.CreateNewSession(newSessionTitle.trim()))
                            showNewSessionDialog = false
                            newSessionTitle = ""
                        }
                    },
                    enabled = newSessionTitle.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNewSessionDialog = false
                    newSessionTitle = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SessionItem(
    session: ChatSession,
    isSelected: Boolean,
    onSessionClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStyleChange: (ResponseStyle) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSessionClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Session",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Response style selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Style: ${session.responseStyle.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text("Change")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ResponseStyle.values().forEach { style ->
                            DropdownMenuItem(
                                text = { Text(style.displayName) },
                                onClick = {
                                    onStyleChange(style)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Message count
            Text(
                text = "${session.messages.size} messages",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}