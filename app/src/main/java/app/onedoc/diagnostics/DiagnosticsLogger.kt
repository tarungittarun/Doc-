package app.onedoc.diagnostics

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * DiagnosticsLogger — captures every app interaction, log, and stat.
 *
 * Logs are kept in memory and can be exported to a local file via
 * [exportToFile]. The file can be saved and shared for analysis.
 *
 * Log levels:
 *  - INFO: normal operations (app start, navigation)
 *  - WARN: unexpected but non-fatal conditions
 *  - ERROR: failures and crashes
 *  - DEBUG: detailed interaction traces (clicks, text edits)
 */
class DiagnosticsLogger(private val context: Context) {

    private val logs = ConcurrentLinkedQueue<DiagnosticEntry>()
    private var startTime: Long = System.currentTimeMillis()
    private var clickCount: Int = 0
    private var documentCount: Int = 0
    private var errorCount: Int = 0

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        val entry = DiagnosticEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message,
            throwable = throwable,
        )
        logs.add(entry)

        if (level == LogLevel.ERROR) errorCount++
        android.util.Log.println(
            when (level) {
                LogLevel.ERROR -> android.util.Log.ERROR
                LogLevel.WARN -> android.util.Log.WARN
                LogLevel.INFO -> android.util.Log.INFO
                LogLevel.DEBUG -> android.util.Log.DEBUG
            },
            tag,
            message + (if (throwable != null) "\n${Log.getStackTraceString(throwable)}" else "")
        )
    }

    fun info(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun warn(tag: String, message: String) = log(LogLevel.WARN, tag, message)
    fun error(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.ERROR, tag, message, throwable)
    fun debug(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)

    fun recordClick(element: String) {
        clickCount++
        debug("DIAGNOSTICS", "CLICK: $element")
    }

    fun recordDocumentCreated() {
        documentCount++
        info("DIAGNOSTICS", "Document created. Total: $documentCount")
    }

    fun recordNavigation(from: String, to: String) {
        info("DIAGNOSTICS", "NAVIGATION: $from -> $to")
    }

    fun recordCrash(throwable: Throwable, context: String) {
        error("CRASH", "Crash in $context: ${throwable.message}", throwable)
    }

    fun getSnapshot(): DiagnosticSnapshot {
        return DiagnosticSnapshot(
            startTime = startTime,
            capturedAt = System.currentTimeMillis(),
            clickCount = clickCount,
            documentCount = documentCount,
            errorCount = errorCount,
            entries = logs.toList(),
        )
    }

    /**
     * Export all logs to a file in the app's external files directory (Documents).
     * Returns the file path, or null on failure.
     */
    fun exportToFile(): String? {
        val snapshot = getSnapshot()
        val fileName = "onedoc_diagnostics_${dateFormat.format(Date())}.log"
        val file = File(context.externalFilesDirs.firstOrNull() ?: context.filesDir, fileName)

        try {
            file.bufferedWriter().use { writer ->
                writer.write("=== OneDoc Diagnostics Report ===")
                writer.newLine()
                writer.write("Generated: ${dateFormat.format(Date(snapshot.capturedAt))}")
                writer.newLine()
                writer.write("App started: ${dateFormat.format(Date(snapshot.startTime))}")
                writer.newLine()
                writer.write("Uptime (ms): ${snapshot.capturedAt - snapshot.startTime}")
                writer.newLine()
                writer.write("Total clicks: ${snapshot.clickCount}")
                writer.newLine()
                writer.write("Documents created: ${snapshot.documentCount}")
                writer.newLine()
                writer.write("Errors: ${snapshot.errorCount}")
                writer.newLine()
                writer.write("=== Log Entries ===")
                writer.newLine()
                writer.newLine()

                for (entry in snapshot.entries) {
                    val time = dateFormat.format(Date(entry.timestamp))
                    val throwableStr = if (entry.throwable != null)
                        "\n${Log.getStackTraceString(entry.throwable)}" else ""
                    writer.write("[$time] [${entry.level}] [${entry.tag}] ${entry.message}$throwableStr")
                    writer.newLine()
                    writer.newLine()
                }
            }
            return file.absolutePath
        } catch (e: Exception) {
            error("DIAGNOSTICS", "Failed to export diagnostics: ${e.message}", e)
            return null
        }
    }
}

enum class LogLevel { INFO, WARN, ERROR, DEBUG }

data class DiagnosticEntry(
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable?,
)

data class DiagnosticSnapshot(
    val startTime: Long,
    val capturedAt: Long,
    val clickCount: Int,
    val documentCount: Int,
    val errorCount: Int,
    val entries: List<DiagnosticEntry>,
)
