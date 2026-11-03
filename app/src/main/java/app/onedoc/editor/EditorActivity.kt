package app.onedoc.editor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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

        setContent {
            OneDocTheme {
                EditorScreen(
                    documentId = documentId,
                    isNew = isNew,
                )
            }
        }
    }
}

@Composable
fun EditorScreen(documentId: String, isNew: Boolean) {
    val coroutineScope = rememberCoroutineScope()
    val repository = DocumentRepository.getInstance(androidx.compose.ui.platform.LocalContext.current)
    val localContext = androidx.compose.ui.platform.LocalContext.current

    var docState by remember { mutableStateOf<Document?>(null) }

    if (docState == null) {
        coroutineScope.launch {
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
                repository.createDocument(loaded)
                app.onedoc.diagnostics.DiagnosticsLogger(localContext).recordDocumentCreated()
            } else {
                loaded = repository.getDocument(documentId) ?: Document(
                    id = documentId,
                    title = "Untitled document",
                    blocks = listOf(Block.Paragraph(id = blockId(), text = "")),
                    version = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            }
            docState = loaded
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = docState?.title ?: "Untitled", style = MaterialTheme.typography.headlineSmall)
        Divider()

        if (docState != null) {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            ) {
                items(items = docState!!.blocks, key = { it.id }) { block ->
                    when (block) {
                        is Block.Paragraph -> {
                            val textState = remember(block.id) { mutableStateOf(block.text) }
                            TextField(
                                value = textState.value,
                                onValueChange = { newText ->
                                    textState.value = newText
                                    val updatedBlocks = docState!!.blocks.map { b ->
                                        if (b.id == block.id)
                                            b.copy(text = newText, updatedAt = System.currentTimeMillis())
                                        else b
                                    }
                                    docState = docState!!.copy(
                                        blocks = updatedBlocks,
                                        updatedAt = System.currentTimeMillis(),
                                        version = docState!!.version + 1,
                                    )
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
                    val doc = docState
                    if (doc != null) {
                        coroutineScope.launch {
                            repository.createDocument(doc)
                            app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("EDITOR", "Saved document ${doc.id}")
                        }
                    }
                },
            ) {
                Text(text = "Save")
            }
            Button(
                onClick = {
                    app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("EDITOR", "Diagnostics requested")
                    val intent = app.onedoc.diagnostics.DiagnosticsActivity.createIntent(localContext)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    localContext.startActivity(intent)
                },
            ) {
                Text(text = "Diagnostics")
            }
        }
    }
}

private fun blockId() = java.util.UUID.randomUUID().toString()