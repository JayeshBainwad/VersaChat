package com.jsb.versachat.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jsb.versachat.domain.model.Message
import com.jsb.versachat.domain.model.MessageRole
import com.jsb.versachat.domain.model.ResponseStyle
import com.jsb.versachat.presentation.ui.state.ChatUiEvent
import com.jsb.versachat.presentation.viewmodel.ChatViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.delay
import com.jsb.versachat.presentation.ui.components.MessageBubble
import com.jsb.versachat.presentation.ui.components.MessageWithActions
import com.jsb.versachat.presentation.ui.components.ResponseStyleSelector
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle error display
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.onEvent(ChatUiEvent.ClearError)
        }
    }

    // Handle drawer state
    LaunchedEffect(uiState.isDrawerOpen) {
        if (uiState.isDrawerOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    // Show loading indicator while initial data is being loaded
    if (uiState.isLoading && uiState.sessions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
            ) {
                ChatListScreen(
                    sessions = uiState.sessions,
                    currentSessionId = uiState.currentSessionId,
                    onEvent = viewModel::onEvent
                )
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.currentSession?.title ?: "VersaChat",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            if (!uiState.hasAnySessions) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    // Response Style Selector
                    uiState.currentSession?.let { session ->
                        ResponseStyleSelector(
                            currentStyle = session.responseStyle,
                            onStyleChanged = { newStyle ->
                                viewModel.onEvent(
                                    ChatUiEvent.UpdateResponseStyle(session.id, newStyle)
                                )
                            },
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Messages list
                    val listState = rememberLazyListState()
                    val messages = uiState.currentSession?.messages ?: emptyList()

                    // Auto-scroll to bottom when new messages arrive
                    LaunchedEffect(messages.size) {
                        if (messages.isNotEmpty()) {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = messages, key = { it.timestamp }) { message ->
                            MessageWithActions(
                                message = message,
                                currentResponseStyle = uiState.currentSession?.responseStyle
                                    ?: ResponseStyle.DETAILED,
                                onRegenerateResponse = { newStyle ->
                                    viewModel.onEvent(
                                        ChatUiEvent.RegenerateLastResponse(newStyle)
                                    )
                                }
                            )
                        }

                        // Loading indicator for AI response
                        if (uiState.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "AI is typing...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input area
                    MessageInput(
                        onSendMessage = { message ->
                            viewModel.onEvent(ChatUiEvent.SendMessage(message))
                            keyboardController?.hide()
                        },
                        enabled = !uiState.isLoading
                    )
                }
            }
        }
    }
}


@Composable
private fun MessageInput(
    onSendMessage: (String) -> Unit,
    enabled: Boolean = true
) {
    var input by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
            enabled = enabled,
            maxLines = 4
        )

        Spacer(modifier = Modifier.width(8.dp))

        FilledIconButton(
            onClick = {
                if (input.isNotBlank()) {
                    onSendMessage(input.trim())
                    input = ""
                }
            },
            enabled = enabled && input.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send"
            )
        }
    }
}