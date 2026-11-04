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
import androidx.compose.runtime.LaunchedEffect
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

    // Document state — only changes when user clicks Save or document is loaded
    val docState: MutableState<Document?> = remember { mutableStateOf<Document?>(null) }

    // Local editing state — updated on every keystroke, does NOT trigger document recomposition
    val editTextState: MutableState<String> = remember { mutableStateOf("") }
    val editingBlockIdState: MutableState<String> = remember { mutableStateOf("") }

    // Load document on first composition
    LaunchedEffect(Unit) {
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
                // Initialize editing state from loaded document
                val firstPara = loaded.blocks.firstOrNull { it is Block.Paragraph } as? Block.Paragraph
                editTextState.value = firstPara?.text ?: ""
                editingBlockIdState.value = firstPara?.id ?: ""
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
                            // Only use local editText state for the block being edited;
                            // for other blocks, show their stored text
                            val isEditingBlock = block.id == editingBlockIdState.value
                            val displayText = if (isEditingBlock) editTextState.value else block.text
                            TextField(
                                value = displayText,
                                onValueChange = { newText ->
                                    // Update only local state — no document recomposition per keystroke
                                    editTextState.value = newText
                                    editingBlockIdState.value = block.id
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
                        val current = docState.value
                        if (current != null) {
                            // Persist the current editText into the document before saving
                            val updatedBlocks = current.blocks.map { b ->
                                if (b.id == editingBlockIdState.value && b is Block.Paragraph)
                                    b.copy(text = editTextState.value)
                                else b
                            }
                            val docToSave = current.copy(
                                blocks = updatedBlocks,
                                updatedAt = System.currentTimeMillis(),
                                version = current.version + 1,
                            )
                            coroutineScope.launch {
                                try {
                                    val repo = DocumentRepository.getInstance(localContext)
                                    repo.createDocument(docToSave)
                                    docState.value = docToSave
                                    editingBlockIdState.value = docToSave.blocks.firstOrNull { it is Block.Paragraph }?.id ?: ""
                                    editTextState.value = docToSave.blocks.firstOrNull { it is Block.Paragraph }?.text ?: ""
                                    app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("EDITOR", "Saved document ${docToSave.id}")
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
