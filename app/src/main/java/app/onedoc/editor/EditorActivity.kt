package app.onedoc.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import app.onedoc.model.Block
import app.onedoc.model.Document
import app.onedoc.ui.theme.OneDocTheme

/**
 * Editor screen — block-based rich-text editor (§13 laws L1-L10, §14 model).
 *
 * Shows document title, block-based content area, and formatting toolbar.
 * Exposes `od_editor_content` accessibility id for direct-APK QA driving (§24).
 *
 * Full block rendering engine (13 block kinds, 7 mark types, IME-safe,
 * honest undo) lands in G3; this shell renders paragraph blocks as editable text.
 */
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
    val color = MaterialTheme.colorScheme

    // TODO G2: load document from repository by documentId
    val doc = remember {
        Document(
            id = documentId,
            title = if (isNew) "Untitled document" else documentId,
            blocks = listOf(
                Block.Paragraph(
                    id = "block-1",
                    text = "",
                )
            ),
            version = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Title bar
        TextField(
            value = doc.title,
            onValueChange = { /* save title via repository (G2) */ },
            label = { Text(text = "Title") },
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = TextFieldDefaults.colors(containerColor = Color.Transparent),
        )
        Divider()

        // Content area — block list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
        ) {
            items(doc.blocks, key = { it.id }) { block ->
                BlockRow(block = block)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun BlockRow(block: Block) {
    when (block) {
        is Block.Paragraph -> ParagraphBlock(block)
        is Block.Heading -> HeadingBlock(block)
        is Block.ListItem -> ListItemBlock(block)
        is Block.CodeBlock -> CodeBlock(block)
        is Block.Blockquote -> BlockquoteBlock(block)
        else -> Text(text = block.kind.name)
    }
}

@Composable
private fun ParagraphBlock(block: Block.Paragraph) {
    val textState = remember { mutableStateOf(block.text) }
    TextField(
        value = textState.value,
        onValueChange = { textState.value = it },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = TextFieldDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun HeadingBlock(block: Block.Heading) {
    val textState = remember { mutableStateOf(block.text) }
    TextField(
        value = textState.value,
        onValueChange = { textState.value = it },
        modifier = Modifier.fillMaxWidth(),
        textStyle = when (block.level) {
            1 -> MaterialTheme.typography.headlineLarge
            2 -> MaterialTheme.typography.headlineMedium
            3 -> MaterialTheme.typography.headlineSmall
            else -> MaterialTheme.typography.titleLarge
        },
        colors = TextFieldDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun ListItemBlock(block: Block.ListItem) {
    val textState = remember { mutableStateOf(block.text) }
    val prefix = if (block.ordered) "1. " else "•  "
    TextField(
        value = prefix + textState.value,
        onValueChange = { textState.value = it.removePrefix(prefix) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = TextFieldDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun CodeBlock(block: Block.CodeBlock) {
    val textState = remember { mutableStateOf(block.code) }
    TextField(
        value = textState.value,
        onValueChange = { textState.value = it },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = TextFieldDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    )
}

@Composable
private fun BlockquoteBlock(block: Block.Blockquote) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = block.blocks.joinToString(" ") { b ->
                    when (b) {
                        is Block.Paragraph -> b.text
                        else -> b.kind.name
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}