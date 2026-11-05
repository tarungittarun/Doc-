package app.onedoc.diagnostics

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import app.onedoc.ui.theme.OneDocTheme
import app.onedoc.ui.components.AppTopBar

/**
 * DiagnosticsActivity — shows captured diagnostics logs and allows export to file.
 *
 * Users can save the diagnostics file to local storage (Documents/Downloads) and
 * share it for analysis. Captures every interaction, log, and stat.
 */
class DiagnosticsActivity : ComponentActivity() {

    private lateinit var logger: DiagnosticsLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger = DiagnosticsLogger(applicationContext)
        logger.info("DIAGNOSTICS", "DiagnosticsActivity opened")

        setContent {
            OneDocTheme {
                DiagnosticsScreen(
                    logger = logger,
                    onExport = { exportLogs() },
                    onClear = { logger.getSnapshot(); /* entries cleared on new log */ },
                )
            }
        }
    }

    private fun exportLogs() {
        logger.exportToFile { path ->
            if (path != null) {
                val file = File(path)
                logger.info("DIAGNOSTICS", "Diagnostics exported to $path")

                val uri = FileProvider.getUriForFile(
                    applicationContext,
                    "app.onedoc.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "text/plain")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }

                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    // Fallback: save to Downloads
                    saveToDownloads(file)
                }
            }
        }
    }

    private fun saveToDownloads(file: File) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val dest = File(downloadsDir, file.name)
        try {
            file.copyTo(dest, overwrite = true)
            logger.info("DIAGNOSTICS", "Diagnostics saved to Downloads: ${dest.absolutePath}")
        } catch (e: Exception) {
            logger.error("DIAGNOSTICS", "Failed to save to Downloads: ${e.message}", e)
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, DiagnosticsActivity::class.java)
        }
    }
}

@Composable
fun DiagnosticsScreen(
    logger: DiagnosticsLogger,
    onExport: () -> Unit,
    onClear: () -> Unit,
) {
    val snapshot = remember { mutableStateOf(logger.getSnapshot()) }

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = "Diagnostics",
            onMenuClick = { },
            onSearchClick = { },
            modifier = Modifier,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            StatCard(
                "Uptime",
                formatUptime(snapshot.value.capturedAt - snapshot.value.startTime),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                "Clicks",
                snapshot.value.clickCount.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                "Documents",
                snapshot.value.documentCount.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                "Errors",
                snapshot.value.errorCount.toString(),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = onExport) {
                Text(text = "Export to File")
            }
            Button(onClick = onClear) {
                Text(text = "Clear Logs")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Log Entries",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        // Log list
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            items(snapshot.value.entries) { entry ->
                LogEntry(
                    entry = entry,
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.height(80.dp),
        color = color.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = color.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color.onSurface,
            )
        }
    }
}

@Composable
private fun LogEntry(entry: DiagnosticEntry) {
    val color = MaterialTheme.colorScheme
    val levelColor = when (entry.level) {
        LogLevel.ERROR -> color.error
        LogLevel.WARN -> Color(0xFFFFA000) // amber
        LogLevel.INFO -> color.primary
        LogLevel.DEBUG -> color.onSurfaceVariant
    }

    val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    val time = timeFormat.format(Date(entry.timestamp))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = color.surfaceContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "[${entry.level}]",
                    style = MaterialTheme.typography.bodySmall,
                    color = levelColor,

                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.tag,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodyMedium,
                color = color.onSurface,
            )
            if (entry.throwable != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Log.getStackTraceString(entry.throwable),
                    style = MaterialTheme.typography.bodySmall,
                    color = color.error,
                )
            }
        }
    }
}

private fun formatUptime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60)) % 24
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}
