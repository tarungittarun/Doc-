package app.onedoc.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import app.onedoc.ui.theme.OneDocTheme

/**
 * Settings activity stub (G2 T-FOUND-015 DataStore SettingsRegistry).
 *
 * Theme toggle, font scale, reduced motion, locale. DataStore-backed typed accessors
 * land in G2; this stub renders a placeholder.
 */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneDocTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = "od_settings_stub" }
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        }
    }
}
