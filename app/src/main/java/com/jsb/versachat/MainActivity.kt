package com.jsb.versachat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jsb.versachat.presentaion.ui.screen.ChatScreen
import com.jsb.versachat.presentaion.ui.screen.ChatViewModel
import com.jsb.versachat.ui.theme.VersaChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val viewModel: ChatViewModel by viewModels()
                ChatScreen(viewModel = viewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VersaChatTheme {
        ChatScreen(viewModel = ChatViewModel())
    }
}