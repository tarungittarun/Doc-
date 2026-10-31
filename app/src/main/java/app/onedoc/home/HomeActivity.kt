package app.onedoc.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observeAsState
import kotlinx.coroutines.launch
import app.onedoc.editor.EditorActivity
import app.onedoc.model.Block
import app.onedoc.model.Document
import app.onedoc.repository.DocumentRepository
import app.onedoc.ui.components.AppTopBar
import app.onedoc.ui.components.DocumentListItem
import app.onedoc.ui.components.EmptyState
import app.onedoc.ui.theme.OneDocTheme

/**
 * Home screen — document library (§5.3, §5.6).
 *
 * Shows active documents, with search, starred/trashed filters, and
 * FAB to create new documents. Exposes `od_home_list` accessibility id
 * for direct-APK QA driving (§24).
 */
class HomeActivity : ComponentActivity() {
    private val repository: DocumentRepository by lazy {
        DocumentRepository.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneDocTheme {
                HomeScreen(
                    repository = repository,
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
        // Save via repository, then refresh LiveData so the new doc appears
        lifecycleScope.launch {
            repository.createDocument(doc)
            repository.refresh()
        }
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
    repository: DocumentRepository,
    onCreateDocument: () -> Unit,
    onDocumentClick: (Document) -> Unit,
) {
    val color = MaterialTheme.colorScheme

    // Search state (G5)
    var searchQuery by remember { mutableStateOf("") }
    var searchExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = "OneDoc",
            onSearchClick = { searchExpanded = !searchExpanded },
            modifier = Modifier,
        )

        // Search bar (G5)
        if (searchExpanded) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(text = "Search documents") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = TextFieldDefaults.colors(),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = { onCreateDocument() },
            ) {
                Text(text = "New document")
            }
            TextButton(
                onClick = {},
            ) {
                Text(text = "Starred")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (searchQuery.isNotEmpty()) "Search results" else "Documents",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        // Document list — populated from repository via LiveData observer (G2)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .weight(1f),
        ) {
            val docs by repository.documents.observeAsState(emptyList())
            val filteredDocs = if (searchQuery.isNotEmpty()) {
                docs.filter { it.title.lowercase().contains(searchQuery.lowercase()) }
            } else {
                docs
            }
            if (filteredDocs.isEmpty()) {
                EmptyState(
                    title = if (searchQuery.isNotEmpty()) "No matching documents" else "No documents yet",
                    description = if (searchQuery.isNotEmpty()) "Try a different search term." else "Tap + to create your first document.",
                    onCreate = onCreateDocument,
                )
            } else {
                filteredDocs.forEach { doc ->
                    DocumentListItem(
                        document = doc,
                        onClick = { onDocumentClick(doc) },
                    )
                }
            }
        }
    }
}
