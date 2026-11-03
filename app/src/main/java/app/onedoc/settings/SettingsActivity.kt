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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
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
    val color = MaterialTheme.colorScheme
    val localContext = LocalContext.current
    val prefs: SharedPreferences = localContext.getSharedPreferences("onedoc_settings", Context.MODE_PRIVATE)

    var darkTheme by remember { mutableStateOf(prefs.getBoolean("dark_theme", false)) }
    var fontSizeScale by remember { mutableStateOf(prefs.getInt("font_size_scale", 2)) }
    var reducedMotion by remember { mutableStateOf(prefs.getBoolean("reduced_motion", false)) }

    Column(modifier = Modifier.fillMaxSize()) {
        app.onedoc.ui.components.AppTopBar(
            title = "Settings",
            onMenuClick = { /* back handled by system */ },
            onSearchClick = { },
            modifier = Modifier,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Theme section
            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            item {
                SettingsItem(
                    title = "Dark theme",
                    description = "Switch between light and dark themes",
                    checked = darkTheme,
                    onCheckedChange = { checked ->
                        darkTheme = checked
                        prefs.edit().putBoolean("dark_theme", checked).apply()
                        app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("SETTINGS", "Dark theme -> $checked")
                    },
                )
            }
            item {
                SettingsItem(
                    title = "Font size",
                    description = "Small / Medium / Large text scaling",
                    onClick = {
                        fontSizeScale = when (fontSizeScale) {
                            1 -> 2
                            2 -> 3
                            else -> 1
                        }
                        prefs.edit().putInt("font_size_scale", fontSizeScale).apply()
                        app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("SETTINGS", "Font size -> $fontSizeScale")
                    },
                )
            }
            item {
                SettingsItem(
                    title = "Reduced motion",
                    description = "Minimize animation",
                    checked = reducedMotion,
                    onCheckedChange = { checked ->
                        reducedMotion = checked
                        prefs.edit().putBoolean("reduced_motion", checked).apply()
                    },
                )
            }
            item {
                SettingsItem(
                    title = "Language",
                    description = "English / Hindi / Marathi",
                    onClick = {
                        app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("SETTINGS", "Language clicked")
                    },
                )
            }

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // About section
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            item {
                SettingsItem(
                    title = "Version",
                    description = "OneDoc v1.0.4 (build 5)",
                )
            }
            item {
                SettingsItem(
                    title = "Diagnostics",
                    description = "View logs, export diagnostics file",
                    onClick = {
                        val intent = app.onedoc.diagnostics.DiagnosticsActivity.createIntent(localContext)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        localContext.startActivity(intent)
                    },
                )
            }
            item {
                SettingsItem(
                    title = "Storage usage",
                    description = "App data and document storage",
                )
            }
            item {
                SettingsItem(
                    title = "Self-test",
                    description = "Run offline diagnostics self-test",
                    onClick = {
                        app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("SETTINGS", "Self-test requested")
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String = "",
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    onClick: () -> Unit = {},
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        leadingContent = {
            if (onCheckedChange != {}) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                )
            }
        },
        headlineContent = {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            if (description.isNotEmpty()) {
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
        },
        colors = ListItemDefaults.colors(),
    )
}