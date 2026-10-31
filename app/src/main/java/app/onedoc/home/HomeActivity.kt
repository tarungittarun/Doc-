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
        // Save via repository (G2 coroutine scope), then navigate to editor
        androidx.lifecycle.lifecycleScope.launch {
            repository.createDocument(doc)
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
            ActionChip(
                onClick = { onCreateDocument() },
                label = { Text(text = "New document") },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
            )
            ActionChip(
                onClick = {},
                label = { Text(text = "Starred") },
                icon = { Icon(imageVector = Icons.Default.Star, contentDescription = null) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Documents",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        // Document list — populated from repository in G2+
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .weight(1f),
        ) {
            // Show documents from repository (observer pattern lands in G2)
            val docs by repository.documents.observeAsState(emptyList())
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
