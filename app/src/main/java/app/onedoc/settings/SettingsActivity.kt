package app.onedoc.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.onedoc.ui.theme.OneDocTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneDocTheme {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        app.onedoc.ui.components.AppTopBar(
            title = "Settings",
            modifier = Modifier,
        )
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            SettingsItem(
                title = "Dark theme",
                description = "Switch between light and dark themes",
                checked = false,
                onCheckedChange = {},
            )
            SettingsItem(
                title = "Font size",
                description = "Small / Medium / Large",
            )
            SettingsItem(
                title = "Reduced motion",
                description = "Minimize animation",
                checked = false,
                onCheckedChange = {},
            )
            SettingsItem(
                title = "Language",
                description = "English / Hindi / Marathi",
            )
            Divider()
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
) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {
            if (checked) {
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
