package app.onedoc.editor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import app.onedoc.diagnostics.DiagnosticsLogger
import app.onedoc.model.Block
import app.onedoc.model.Document
import app.onedoc.repository.DocumentRepository
import app.onedoc.ui.theme.OneDocTheme

class EditorActivity : ComponentActivity() {

    private var documentId: String = ""
    private var isNew: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentId = intent.getStringExtra("documentId") ?: ""
        isNew = intent.getBooleanExtra("isNew", false)

        val diagnostics = app.onedoc.diagnostics.DiagnosticsLogger(this)
        diagnostics.recordNavigation("HomeActivity", "EditorActivity")

        try {
            setContent {
                OneDocTheme {
                    EditorScreen(
                        documentId = documentId,
                        isNew = isNew,
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("EditorActivity", "Crash in onCreate setContent", e)
            diagnostics.error("EDITOR", "Crash in onCreate setContent", e)
            // Show a minimal error screen instead of crashing
            setContent {
                OneDocTheme {
                    Text(
                        text = "Editor failed to load. See Diagnostics log.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun EditorScreen(documentId: String, isNew: Boolean) {
    val coroutineScope = rememberCoroutineScope()
    val localContext = androidx.compose.ui.platform.LocalContext.current

    // Use explicit .value access instead of 'by' delegate to avoid Kotlin delegate ambiguity
    val docState: MutableState<Document?> = remember { mutableStateOf<Document?>(null) }

    if (docState.value == null) {
        coroutineScope.launch {
            try {
                val loaded: Document
                if (isNew || documentId.isEmpty()) {
                    loaded = Document(
                        id = blockId(),
                        title = "Untitled document",
                        blocks = listOf(Block.Paragraph(id = blockId(), text = "")),
                        version = 1,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                    val repo = DocumentRepository.getInstance(localContext)
                    repo.createDocument(loaded)
                    app.onedoc.diagnostics.DiagnosticsLogger(localContext).recordDocumentCreated()
                } else {
                    val repo = DocumentRepository.getInstance(localContext)
                    loaded = repo.getDocument(documentId) ?: Document(
                        id = documentId,
                        title = "Untitled document",
                        blocks = listOf(Block.Paragraph(id = blockId(), text = "")),
                        version = 1,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                }
                docState.value = loaded
            } catch (e: Exception) {
                Log.e("EditorActivity", "Failed to load document", e)
                app.onedoc.diagnostics.DiagnosticsLogger(localContext).error("EDITOR", "Failed to load document", e)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = docState.value?.title ?: "Untitled", style = MaterialTheme.typography.headlineSmall)
        Divider()

        val currentDoc = docState.value
        if (currentDoc != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            ) {
                items(currentDoc.blocks, key = { it.id }) { block ->
                    when (block) {
                        is Block.Paragraph -> {
                            val textState = remember(block.id) { mutableStateOf(block.text) }
                            TextField(
                                value = textState.value,
                                onValueChange = { newText ->
                                    try {
                                        textState.value = newText
                                        val updatedBlocks = currentDoc.blocks.map { b ->
                                            if (b.id == block.id && b is Block.Paragraph)
                                                b.copy(text = newText)
                                            else b
                                        }
                                        docState.value = currentDoc.copy(
                                            blocks = updatedBlocks,
                                            updatedAt = System.currentTimeMillis(),
                                            version = currentDoc.version + 1,
                                        )
                                    } catch (e: Exception) {
                                        Log.e("EditorActivity", "Failed to update text", e)
                                        app.onedoc.diagnostics.DiagnosticsLogger(localContext).error("EDITOR", "Failed to update text", e)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        else -> Text(text = block.kind.name)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    try {
                        val doc = docState.value
                        if (doc != null) {
                            coroutineScope.launch {
                                try {
                                    val repo = DocumentRepository.getInstance(localContext)
                                    repo.createDocument(doc)
                                    app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("EDITOR", "Saved document ${doc.id}")
                                } catch (e: Exception) {
                                    Log.e("EditorActivity", "Failed to save document", e)
                                    app.onedoc.diagnostics.DiagnosticsLogger(localContext).error("EDITOR", "Failed to save document", e)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("EditorActivity", "Save button click failed", e)
                        app.onedoc.diagnostics.DiagnosticsLogger(localContext).error("EDITOR", "Save button click failed", e)
                    }
                },
            ) {
                Text(text = "Save")
            }
            Button(
                onClick = {
                    try {
                        app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("EDITOR", "Diagnostics requested")
                        val intent = app.onedoc.diagnostics.DiagnosticsActivity.createIntent(localContext)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        localContext.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("EditorActivity", "Failed to open diagnostics", e)
                        app.onedoc.diagnostics.DiagnosticsLogger(localContext).error("EDITOR", "Failed to open diagnostics", e)
                    }
                },
            ) {
                Text(text = "Diagnostics")
            }
        }
    }
}

private fun blockId() = java.util.UUID.randomUUID().toString()
