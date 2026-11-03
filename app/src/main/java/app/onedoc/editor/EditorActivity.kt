package app.onedoc.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class EditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneDocTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(text = "Editor", style = MaterialTheme.typography.headlineLarge)
                }
            }
        }
    }
}