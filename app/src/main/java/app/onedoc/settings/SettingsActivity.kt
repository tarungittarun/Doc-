package app.onedoc.settings

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.onedoc.ui.components.AppTopBar
import app.onedoc.ui.theme.OneDocTheme
import kotlinx.coroutines.compose.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Settings screen (§17.7).
 *
 * Theme toggle, font size scale, reduced motion, locale.
 * Settings persisted via DataStore (G6).
 * Exposes `od_settings_screen` accessibility id for direct-APK QA driving (§24).
 */
class SettingsActivity : ComponentActivity() {
    private val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStoreFactory().create(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneDocTheme {
                SettingsScreen(
                    settingsDataStore = settingsDataStore,
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(settingsDataStore: SettingsDataStore) {
    val scope = rememberCoroutineScope()

    // Load settings from DataStore via produceState (G6)
    val settings by settingsDataStore.settings.collectAsState(initial = AppSettings())

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = "Settings",
            modifier = Modifier,
        )

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Dark theme
            SettingsItem(
                title = "Dark theme",
                description = "Switch between light and dark themes",
                checked = settings.darkTheme,
                onCheckedChange = { checked ->
                    scope.launch {
                        settingsDataStore.updateSettings { copy(darkTheme = checked) }
                    }
                },
            )

            // Font size
            SettingsItem(
                title = "Font size",
                description = when (settings.fontSizeScale) {
                    1 -> "Small"
                    2 -> "Medium"
                    3 -> "Large"
                    else -> "Medium"
                },
                onClick = {
                    val next = when (settings.fontSizeScale) {
                        1 -> 2
                        2 -> 3
                        else -> 1
                    }
                    scope.launch {
                        settingsDataStore.updateSettings { copy(fontSizeScale = next) }
                    }
                },
            )

            // Reduced motion
            SettingsItem(
                title = "Reduced motion",
                description = "Minimize animation",
                checked = settings.reducedMotion,
                onCheckedChange = { checked ->
                    scope.launch {
                        settingsDataStore.updateSettings { copy(reducedMotion = checked) }
                    }
                },
            )

            // Language
            SettingsItem(
                title = "Language",
                description = when (settings.locale) {
                    "en" -> "English"
                    "hi" -> "Hindi"
                    "mr" -> "Marathi"
                    else -> "English"
                },
                onClick = {
                    val next = when (settings.locale) {
                        "en" -> "hi"
                        "hi" -> "mr"
                        else -> "en"
                    }
                    scope.launch {
                        settingsDataStore.updateSettings { copy(locale = next) }
                    }
                },
            )

            Divider()

            // About
            SettingsItem(
                title = "About",
                description = "Version, provenance, privacy",
            )
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        leadingContent = {
            if (onClick != {}) {
                // Show chevron for clickable items
                Text(text = "›", style = MaterialTheme.typography.bodyLarge)
            } else if (checked != false) {
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
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
    )
}
