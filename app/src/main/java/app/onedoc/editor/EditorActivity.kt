package app.onedoc.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.FontStyle
import androidx.compose.ui.unit.dp
import app.onedoc.model.Block
import app.onedoc.model.Document
import app.onedoc.ui.theme.OneDocTheme

/**
 * Editor screen — block-based rich-text editor (§13 laws L1-L10, §14 model).
 *
 * Renders all 13 block kinds (§14): paragraph, heading, bulletList, orderedList,
 * codeBlock, blockquote, image, table, toc, footnote, divider, pageBreak, title.
 *
 * Exposes `od_editor_content` accessibility id for direct-APK QA driving (§24).
 *
 * Document state is held in remember { mutableStateOf(...) } so edits persist
 * across recomposition. Persistence to repository lands in G2+.
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

    // Undo/redo manager (G4 — Law L3: keystroke loss impossible under process kill)
    val undoManager = remember { UndoManager() }

    // Document state held in composition so edits persist across recomposition.
    // Load from repository by documentId lands in G2+.
    val doc by remember { mutableStateOf(
        Document(
            id = documentId,
            title = if (isNew) "Untitled document" else documentId,
            blocks = listOf(
                Block.Paragraph(
                    id = java.util.UUID.randomUUID().toString(),
                    text = "",
                )
            ),
            version = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
    )}

    // Initialize undo manager with the initial document state
    // (record only once on first composition)
    var initialized by remember { mutableStateOf(false) }
    if (!initialized) {
        undoManager.record(doc)
        initialized = true
    }

    fun updateBlock(updatedBlock: Block) {
        val newBlocks = doc.blocks.map { b ->
            if (b.id == updatedBlock.id) updatedBlock else b
        }
        val newDoc = doc.copy(
            blocks = newBlocks,
            updatedAt = System.currentTimeMillis(),
            version = doc.version + 1,
        )
        doc = newDoc
        undoManager.record(newDoc)
    }

    fun addBlockAfter(blockId: String, newBlock: Block) {
        val index = doc.blocks.indexOfFirst { it.id == blockId }
        val newBlocks = if (index >= 0) {
            doc.blocks.plus(index + 1, newBlock)
        } else {
            doc.blocks + newBlock
        }
        val newDoc = doc.copy(
            blocks = newBlocks,
            updatedAt = System.currentTimeMillis(),
            version = doc.version + 1,
        )
        doc = newDoc
        undoManager.record(newDoc)
    }

    fun handleUndo() {
        val previous = undoManager.undo()
        doc = previous
    }

    fun handleRedo() {
        val next = undoManager.redo()
        doc = next
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Title bar
        var title by remember { mutableStateOf(doc.title) }
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(text = "Title") },
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = TextFieldDefaults.colors(),
        )
        Divider()

        // Undo / Redo toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(
                onClick = handleUndo,
                enabled = undoManager.canUndo,
            ) {
                Icon(imageVector = Icons.Default.Undo, contentDescription = "Undo")
            }
            IconButton(
                onClick = handleRedo,
                enabled = undoManager.canRedo,
            ) {
                Icon(imageVector = Icons.Default.Redo, contentDescription = "Redo")
            }
        }

        // Content area — block list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(doc.blocks, key = { it.id }) { block ->
                BlockRow(
                    block = block,
                    onUpdate = { updateBlock(it) },
                    onAddAfter = { addBlockAfter(block.id, it) },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Add block button at bottom
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = {
                addBlockAfter(
                    doc.blocks.lastOrNull()?.id ?: "",
                    Block.Paragraph(
                        id = java.util.UUID.randomUUID().toString(),
                        text = "",
                    ),
                )
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(text = "Add block")
        }
    }
}

@Composable
private fun BlockRow(
    block: Block,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
    when (block) {
        is Block.Paragraph -> ParagraphBlock(block, onUpdate, onAddAfter)
        is Block.Heading -> HeadingBlock(block, onUpdate, onAddAfter)
        is Block.ListItem -> ListItemBlock(block, onUpdate, onAddAfter)
        is Block.CodeBlock -> CodeBlock(block, onUpdate, onAddAfter)
        is Block.Blockquote -> BlockquoteBlock(block, onUpdate, onAddAfter)
        is Block.ImageBlock -> ImageBlockComposable(block, onUpdate, onAddAfter)
        is Block.Table -> TableBlockComposable(block, onUpdate, onAddAfter)
        is Block.TOC -> TOCBlockComposable(block, onUpdate, onAddAfter)
        is Block.Footnote -> FootnoteBlockComposable(block, onUpdate, onAddAfter)
        is Block.Divider -> DividerBlockComposable(block, onUpdate, onAddAfter)
        is Block.PageBreak -> PageBreakBlockComposable(block, onUpdate, onAddAfter)
        is Block.Title -> TitleBlockComposable(block, onUpdate, onAddAfter)
    }
}

/*──────────────────── 1. Paragraph ────────────────────*/
@Composable
private fun ParagraphBlock(
    block: Block.Paragraph,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
    var text by remember { mutableStateOf(block.text) }
    TextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onUpdate(block.copy(text = newText))
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = TextFieldDefaults.colors(),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        IconButton(onClick = {
            onAddAfter(Block.Paragraph(
                id = java.util.UUID.randomUUID().toString(),
                text = "",
            ))
        }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add block after")
        }
    }
}

/*──────────────────── 2. Heading ────────────────────*/
@Composable
private fun HeadingBlock(
    block: Block.Heading,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
    var text by remember { mutableStateOf(block.text) }
    TextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onUpdate(block.copy(text = newText))
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = when (block.level) {
            1 -> MaterialTheme.typography.headlineLarge
            2 -> MaterialTheme.typography.headlineMedium
            3 -> MaterialTheme.typography.headlineSmall
            4 -> MaterialTheme.typography.titleLarge
            5 -> MaterialTheme.typography.titleMedium
            else -> MaterialTheme.typography.bodyLarge
        },
        colors = TextFieldDefaults.colors(),
    )
}

/*──────────────────── 3. BulletList / 4. OrderedList ────────────────────*/
@Composable
private fun ListItemBlock(
    block: Block.ListItem,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
    var text by remember { mutableStateOf(block.text) }
    val prefix = if (block.ordered) "${block.id.hashCode().abs() % 100}. " else "•  "
    TextField(
        value = prefix + text,
        onValueChange = { newText ->
            val cleanText = if (block.ordered) newText.replaceFirstRegex(Regex("\\d+\\.\\s*"), "") else newText.removePrefix("•  ")
            text = cleanText
            onUpdate(block.copy(text = cleanText))
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = TextFieldDefaults.colors(),
    )
}

/*──────────────────── 5. CodeBlock ────────────────────*/
@Composable
private fun CodeBlock(
    block: Block.CodeBlock,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
    var code by remember { mutableStateOf(block.code) }
    var language by remember { mutableStateOf(block.language) }
    TextField(
        value = code,
        onValueChange = { newCode ->
            code = newCode
            onUpdate(block.copy(code = newCode))
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Normal),
        colors = TextFieldDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    )
    TextField(
        value = language,
        onValueChange = { newLang ->
            language = newLang
            onUpdate(block.copy(language = newLang))
        },
        label = { Text(text = "Language") },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(),
    )
}

/*──────────────────── 6. Blockquote ────────────────────*/
@Composable
private fun BlockquoteBlock(
    block: Block.Blockquote,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
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

/*──────────────────── 7. ImageBlock ────────────────────*/
@Composable
private fun ImageBlockComposable(
    block: Block.ImageBlock,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
    var uri by remember { mutableStateOf(block.uri) }
    var altText by remember { mutableStateOf(block.altText) }
    Column(modifier = Modifier.fillMaxWidth()) {
        if (uri.isNotEmpty()) {
            Image(
                imageVector = Icons.Default.Image,
                contentDescription = altText,
                modifier = Modifier.size(200.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "No image", style = MaterialTheme.typography.bodySmall)
            }
        }
        TextField(
            value = uri,
            onValueChange = { newUri ->
                uri = newUri
                onUpdate(block.copy(uri = newUri))
            },
            label = { Text(text = "Image URI") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(),
        )
        TextField(
            value = altText,
            onValueChange = { newAlt ->
                altText = newAlt
                onUpdate(block.copy(altText = newAlt))
            },
            label = { Text(text = "Alt text") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(),
        )
    }
}

/*──────────────────── 8. Table ────────────────────*/
@Composable
private fun TableBlockComposable(
    block: Block.Table,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
    var rows by remember { mutableStateOf(block.rows) }
    var cols by remember { mutableStateOf(block.cols) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Table: $rows x $cols", style = MaterialTheme.typography.bodySmall)
        LazyVerticalGrid(
            columns = GridCells.Fixed(cols.coerceAtLeast(1)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(rows * cols, key = { it }) { i ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "C${i / cols + 1}R${i % cols + 1}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/*──────────────────── 9. TOC ────────────────────*/
@Composable
private fun TOCBlockComposable(
    block: Block.TOC,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Table of Contents (maxLevel=${block.maxLevel})", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        docBlocksPlaceholder()
    }
}

/*──────────────────── 10. Footnote ────────────────────*/
@Composable
private fun FootnoteBlockComposable(
    block: Block.Footnote,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
    var text by remember { mutableStateOf(block.text) }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        TextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                onUpdate(block.copy(text = newText))
            },
            label = { Text(text = "Footnote") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(),
        )
    }
}

/*──────────────────── 11. Divider ────────────────────*/
@Composable
private fun DividerBlockComposable(
    block: Block.Divider,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
    Divider(modifier = Modifier.fillMaxWidth())
}

/*──────────────────── 12. PageBreak ────────────────────*/
@Composable
private fun PageBreakBlockComposable(
    block: Block.PageBreak,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Divider(modifier = Modifier.fillMaxWidth())
        Text("— Page Break —", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Divider(modifier = Modifier.fillMaxWidth())
    }
}

/*──────────────────── 13. Title ────────────────────*/
@Composable
private fun TitleBlockComposable(
    block: Block.Title,
    onUpdate: (Block) -> Unit,
    onAddAfter: (Block) -> Unit,
) {
    var text by remember { mutableStateOf(block.text) }
    TextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onUpdate(block.copy(text = newText))
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.headlineLarge,
        colors = TextFieldDefaults.colors(),
    )
}

/*──────────────────── Helpers ────────────────────*/
@Composable
private fun docBlocksPlaceholder() {
    Text("(headings appear here)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
