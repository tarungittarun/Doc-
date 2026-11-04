package app.onedoc.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.onedoc.diagnostics.DiagnosticsLogger
import app.onedoc.ui.theme.OneDocTheme

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val diagnostics = app.onedoc.diagnostics.DiagnosticsLogger(this)
        diagnostics.recordNavigation("launcher/Home", "SettingsActivity")

        try {
            setContent {
                OneDocTheme {
                    SettingsScreen(
                        context = this@SettingsActivity,
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SettingsActivity", "Crash in onCreate setContent", e)
            diagnostics.error("SETTINGS", "Crash in onCreate setContent", e)
            // Show a minimal error screen instead of crashing
            setContent {
                OneDocTheme {
                    Text(
                        text = "Settings failed to load. See Diagnostics log.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(context: android.content.Context) {
    val localContext = LocalContext.current
    val prefs: SharedPreferences = localContext.getSharedPreferences("onedoc_settings", Context.MODE_PRIVATE)

    val darkTheme: MutableState<Boolean> = remember { mutableStateOf(prefs.getBoolean("dark_theme", false)) }
    val fontSizeScale: MutableState<Int> = remember { mutableStateOf(prefs.getInt("font_size_scale", 2)) }
    val reducedMotion: MutableState<Boolean> = remember { mutableStateOf(prefs.getBoolean("reduced_motion", false)) }

    Column(modifier = Modifier.fillMaxSize()) {
        app.onedoc.ui.components.AppTopBar(
            title = "Settings",
            modifier = Modifier,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            item {
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    headlineContent = { Text(text = "Version", style = MaterialTheme.typography.bodyLarge) },
                    supportingContent = { Text(text = "OneDoc v1.0.4 (build 5)", style = MaterialTheme.typography.bodyMedium) },
                )
            }
            item {
                ListItem(
                    modifier = Modifier.fillMaxWidth().clickable {
                        try {
                            val intent = app.onedoc.diagnostics.DiagnosticsActivity.createIntent(localContext)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            localContext.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("SettingsActivity", "Failed to open diagnostics", e)
                            app.onedoc.diagnostics.DiagnosticsLogger(localContext).error("SETTINGS", "Failed to open diagnostics", e)
                        }
                    },
                    headlineContent = { Text(text = "Diagnostics", style = MaterialTheme.typography.bodyLarge) },
                    supportingContent = { Text(text = "View logs, export diagnostics file", style = MaterialTheme.typography.bodyMedium) },
                )
            }
            item {
                ListItem(
                    modifier = Modifier.fillMaxWidth().clickable {
                        try {
                            app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("SETTINGS", "Self-test requested")
                        } catch (e: Exception) {
                            Log.e("SettingsActivity", "Self-test failed", e)
                            app.onedoc.diagnostics.DiagnosticsLogger(localContext).error("SETTINGS", "Self-test failed", e)
                        }
                    },
                    headlineContent = { Text(text = "Self-test", style = MaterialTheme.typography.bodyLarge) },
                    supportingContent = { Text(text = "Run offline diagnostics self-test", style = MaterialTheme.typography.bodyMedium) },
                )
            }
        }
    }
}