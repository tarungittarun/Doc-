package app.onedoc.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.onedoc.model.Document

@Composable
fun DocumentListItem(
    document: Document,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val color = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (selected) color.primaryContainer else color.surface,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Updated ${formatTime(document.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = color.onSurface,
                    maxLines = 1,
                )
            }
            if (document.starred) {
                Text(text = "★", style = MaterialTheme.typography.bodyLarge, color = color.primary)
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - millis
    return if (diff < 60000) "Just now"
    else if (diff < 86400000) "${(diff / 3600000).toInt()}h ago"
    else "${(diff / 86400000).toInt()}d ago"
}
