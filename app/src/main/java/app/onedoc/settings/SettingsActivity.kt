package app.onedoc.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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

        setContent {
            OneDocTheme {
                SettingsScreen(
                    context = this@SettingsActivity,
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(context: android.content.Context) {
    val localContext = LocalContext.current
    val prefs: SharedPreferences = localContext.getSharedPreferences("onedoc_settings", Context.MODE_PRIVATE)

    var darkTheme by remember { mutableStateOf(prefs.getBoolean("dark_theme", false)) }

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
                    modifier = Modifier.fillMaxWidth().clickable { },
                    headlineContent = { Text(text = "Diagnostics", style = MaterialTheme.typography.bodyLarge) },
                    supportingContent = { Text(text = "View logs, export diagnostics file", style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        val intent = app.onedoc.diagnostics.DiagnosticsActivity.createIntent(localContext)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        localContext.startActivity(intent)
                    },
                )
            }
            item {
                ListItem(
                    modifier = Modifier.fillMaxWidth().clickable { },
                    headlineContent = { Text(text = "Self-test", style = MaterialTheme.typography.bodyLarge) },
                    supportingContent = { Text(text = "Run offline diagnostics self-test", style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("SETTINGS", "Self-test requested")
                    },
                )
            }
        }
    }
}