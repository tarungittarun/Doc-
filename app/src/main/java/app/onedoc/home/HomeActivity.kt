package app.onedoc.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import app.onedoc.editor.EditorActivity
import app.onedoc.model.Block
import app.onedoc.model.Document
import app.onedoc.repository.DocumentRepository
import app.onedoc.ui.components.AppTopBar
import app.onedoc.ui.components.DocumentListItem
import app.onedoc.ui.components.EmptyState
import app.onedoc.ui.theme.OneDocTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.MainScope

class HomeActivity : ComponentActivity() {
    private val repository: DocumentRepository by lazy {
        DocumentRepository.getInstance(this)
    }

    private val docs: MutableState<List<Document>> = mutableStateOf(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneDocTheme {
                HomeScreen(
                    docs = docs.value,
                    onCreateDocument = { createNewDocument() },
                    onDocumentClick = { doc -> openDocument(doc.id) },
                )
            }
        }
    }

    private fun createNewDocument() {
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
        // Save via repository in background coroutine
        CoroutineScope(Dispatchers.Default).launch {
            repository.createDocument(doc)
        }
        // Navigate to editor
        val intent = Intent(this, EditorActivity::class.java)
        intent.putExtra("documentId", doc.id)
        intent.putExtra("isNew", true)
        startActivity(intent)
    }

    private fun openDocument(documentId: String) {
        val intent = Intent(this, EditorActivity::class.java)
        intent.putExtra("documentId", documentId)
        startActivity(intent)
    }

    private fun blockId() = java.util.UUID.randomUUID().toString()
}

@Composable
fun HomeScreen(
    docs: List<Document>,
    onCreateDocument: () -> Unit,
    onDocumentClick: (Document) -> Unit,
) {
    val color = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = "OneDoc",
            onSearchClick = {},
            modifier = Modifier,
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { onCreateDocument() },
            ) {
                Text(text = "New document")
            }
            Button(
                onClick = {},
            ) {
                Text(text = "Starred")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Documents",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .weight(1f),
        ) {
            if (docs.isEmpty()) {
                EmptyState(
                    title = "No documents yet",
                    description = "Tap + to create your first document.",
                    onCreate = onCreateDocument,
                )
            } else {
                docs.forEach { doc ->
                    DocumentListItem(
                        document = doc,
                        onClick = { onDocumentClick(doc) },
                    )
                }
            }
        }
    }
}
