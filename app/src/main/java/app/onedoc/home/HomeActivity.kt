package app.onedoc.home

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

import app.onedoc.editor.EditorActivity
import app.onedoc.model.Block
import app.onedoc.model.Document
import app.onedoc.repository.DocumentRepository
import app.onedoc.ui.theme.OneDocTheme

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
                )
            }
        }
    }

    private fun createNewDocument() {
        val doc = Document(
            id = blockId(),
            title = "Untitled document",
            blocks = listOf(Block.Paragraph(id = blockId(), text = "")),
            version = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        repository.createDocument(doc)

        val intent = Intent(this, EditorActivity::class.java)
        intent.putExtra("documentId", doc.id)
        intent.putExtra("isNew", true)
        startActivity(intent)
    }

    private fun blockId() = java.util.UUID.randomUUID().toString()
}

@Composable
fun HomeScreen(
    repository: DocumentRepository,
    onCreateDocument: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "OneDoc", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreateDocument) {
            Text(text = "New document")
        }
    }
}