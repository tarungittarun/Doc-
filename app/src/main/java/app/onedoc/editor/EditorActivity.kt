package app.onedoc.editor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = docState?.blocks?.joinToString("\n") { it.kind.name + ": " + (it as? Block.Paragraph)?.text ?: "" } ?: "")
    }
}

private fun blockId() = java.util.UUID.randomUUID().toString()