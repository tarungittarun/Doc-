package app.onedoc.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.onedoc.diagnostics.DiagnosticsActivity
import app.onedoc.settings.SettingsActivity
import app.onedoc.templates.TemplatesActivity

@Composable
fun AppTopBar(
    title: String,
    onMenuClick: () -> Unit = { },
    onSearchClick: () -> Unit = { },
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val showMoreMenu = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
        )
        Row {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                )
            }
            IconButton(onClick = { showMoreMenu.value = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                )
            }
            DropdownMenu(
                expanded = showMoreMenu.value,
                onDismissRequest = { showMoreMenu.value = false },
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Diagnostics") },
                    onClick = {
                        showMoreMenu.value = false
                        val intent = DiagnosticsActivity.createIntent(context)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    },
                )
                DropdownMenuItem(
                    text = { Text(text = "Settings") },
                    onClick = {
                        showMoreMenu.value = false
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    },
                )
                DropdownMenuItem(
                    text = { Text(text = "Templates") },
                    onClick = {
                        showMoreMenu.value = false
                        val intent = Intent(context, TemplatesActivity::class.java)
                        context.startActivity(intent)
                    },
                )
            }
        }
    }
}