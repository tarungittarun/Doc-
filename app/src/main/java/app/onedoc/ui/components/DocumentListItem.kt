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
    onStarToggle: (() -> Unit)? = null,
    onTrash: (() -> Unit)? = null,
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
                    color = color.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            // Star button
            if (onStarToggle != null) {
                IconButton(
                    onClick = onStarToggle,
                ) {
                    Icon(
                        imageVector = if (document.starred) Icons.Filled.Star else Icons.Default.Star,
                        contentDescription = "Star",
                        tint = if (document.starred) color.primary else color.onSurfaceVariant,
                    )
                }
            }

            // Trash / more button
            if (onTrash != null) {
                IconButton(
                    onClick = onTrash,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = color.onSurfaceVariant,
                    )
                }
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