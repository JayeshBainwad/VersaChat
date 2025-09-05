package com.jsb.versachat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jsb.versachat.presentation.ui.screen.ChatScreen
import com.jsb.versachat.ui.theme.VersaChatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity created")

        try {
            enableEdgeToEdge()

            setContent {
                VersaChatTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ChatScreen()
                    }
                }
            }

            Log.d(TAG, "Content set successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            // In a production app, you might want to show an error screen
            // or restart the activity
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "MainActivity started")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity resumed")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity paused")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "MainActivity stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity destroyed")
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    VersaChatTheme {
        // Preview content - you can create a preview version
        Surface {
            // Add preview content here
        }
    }
}