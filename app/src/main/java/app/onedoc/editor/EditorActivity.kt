package app.onedoc.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import app.onedoc.ui.theme.OneDocTheme

/**
 * Editor activity shell (G3 editor core).
 *
 * Block-based rich-text editor (13 block kinds, 7 mark types), IME-safe,
 * honest undo, per Laws L1-L10. Stub here; engine lands in G3.
 */
class EditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneDocTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = "od_editor_shell" }
                ) {
                    Text(
                        text = "Editor",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        }
    }
}
