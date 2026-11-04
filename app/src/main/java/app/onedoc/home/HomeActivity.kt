package app.onedoc.home

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import app.onedoc.diagnostics.DiagnosticsActivity
import app.onedoc.diagnostics.DiagnosticsLogger
import app.onedoc.editor.EditorActivity
import app.onedoc.model.Block
import app.onedoc.model.Document
import app.onedoc.repository.DocumentRepository
import app.onedoc.settings.SettingsActivity
import app.onedoc.templates.TemplatesActivity
import app.onedoc.ui.components.AppTopBar
import app.onedoc.ui.components.DocumentListItem
import app.onedoc.ui.components.EmptyState
import app.onedoc.ui.theme.OneDocTheme

class HomeActivity : ComponentActivity() {

    private val diagnostics by lazy {
        app.onedoc.diagnostics.DiagnosticsLogger(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val launchStart = SystemClock.elapsedRealtime()

        // Record navigation — catch any failure so it doesn't crash app launch
        try {
            diagnostics.recordNavigation("launcher", "HomeActivity")
        } catch (e: Exception) {
            Log.e("HomeActivity", "Failed to record navigation", e)
        }

        try {
            Log.d("HomeActivity", "Cold start: setContent started at ${SystemClock.elapsedRealtime() - launchStart}ms")
            setContent {
                OneDocTheme {
                    HomeScreen(
                        context = this@HomeActivity,
                        onCreateDocument = { createNewDocument() },
                        onDocumentClick = { docId -> openDocument(docId) },
                        onOpenTemplates = { openTemplates() },
                        onOpenSettings = { openSettings() },
                        onOpenDiagnostics = { openDiagnostics() },
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Crash in onCreate setContent", e)
            diagnostics.error("HOME", "Crash in onCreate setContent", e)
            // Show a minimal error screen instead of crashing — always shows "OneDoc"
            setContent {
                OneDocTheme {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text(
                            text = "OneDoc",
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Text(
                            text = "OneDoc failed to start. See Diagnostics log.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
        Log.d("HomeActivity", "Cold start: setContent completed at ${SystemClock.elapsedRealtime() - launchStart}ms")
    }

    private fun createNewDocument() {
        try {
            val doc = Document(
                id = blockId(),
                title = "Untitled document",
                blocks = listOf(
                    Block.Paragraph(
                        id = blockId(),
                        text = "",
                    )
                ),
                version = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
            lifecycleScope.launch {
                try {
                    val repo = DocumentRepository.getInstance(this@HomeActivity)
                    repo.createDocument(doc)
                    diagnostics.recordDocumentCreated()
                    diagnostics.debug("HOME", "Created document ${doc.id}")
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Failed to create document", e)
                    diagnostics.error("HOME", "Failed to create document", e)
                }
            }

            val intent = Intent(this, EditorActivity::class.java)
            intent.putExtra("documentId", doc.id)
            intent.putExtra("isNew", true)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("HomeActivity", "Failed to create new document", e)
            diagnostics.error("HOME", "Failed to create new document", e)
        }
    }

    private fun openDocument(documentId: String) {
        diagnostics.debug("HOME", "Opening document $documentId")
        val intent = Intent(this, EditorActivity::class.java)
        intent.putExtra("documentId", documentId)
        intent.putExtra("isNew", false)
        startActivity(intent)
    }

    private fun openTemplates() {
        val intent = Intent(this, TemplatesActivity::class.java)
        startActivity(intent)
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun openDiagnostics() {
        val intent = DiagnosticsActivity.createIntent(this)
        startActivity(intent)
    }

    private fun blockId() = java.util.UUID.randomUUID().toString()
}

@Composable
fun HomeScreen(
    context: android.content.Context,
    onCreateDocument: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDiagnostics: () -> Unit,
) {
    val color = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = "OneDoc",
            onMenuClick = { onOpenDiagnostics() },
            onSearchClick = { /* search handled by more menu */ },
            modifier = Modifier,
        )
        // Prominent app title visible even if AppTopBar has rendering issues
        Text(
            text = "OneDoc",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { onCreateDocument() },
            ) {
                Row(
                    modifier = Modifier.wrapContentSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = "+", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "New document")
                }
            }
            OutlinedButton(
                onClick = { onOpenTemplates() },
            ) {
                Text(text = "Templates")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                text = "Documents",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = { onOpenSettings() },
            ) {
                Row(
                    modifier = Modifier.wrapContentSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = "⚙", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Settings")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .weight(1f),
        ) {
            EmptyState(
                title = "No documents yet",
                description = "Tap \"New document\" to create your first document.",
                onCreate = onCreateDocument,
            )
        }
    }
}
