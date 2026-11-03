package app.onedoc.editor

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
                    context = this@EditorActivity,
                    documentId = documentId,
                    isNew = isNew,
                )
            }
        }
    }
}

@Composable
fun EditorScreen(
    context: android.content.Context,
    documentId: String,
    isNew: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()
    val repository = DocumentRepository.getInstance(context)
    val localContext = LocalContext.current
    val color = MaterialTheme.colorScheme

    // --- Document state ---
    var docState by remember { mutableStateOf<Document?>(null) }
    var initialized by remember { mutableStateOf(false) }

    // Load document on first composition
    if (!initialized) {
        initialized = true
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
                app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("EDITOR", "Created new document ${loaded.id}")
            } else {
                loaded = repository.getDocument(documentId) ?: Document(
                    id = documentId,
                    title = "Untitled document",
                    blocks = listOf(Block.Paragraph(id = blockId(), text = "")),
                    version = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
                app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("EDITOR", "Opened document $documentId")
            }
            docState = loaded
        }
    }

    // --- Auto-save: persist to repository whenever docState changes ---
    if (docState != null) {
        androidx.compose.runtime.LaunchedEffect(docState?.version) {
            val doc = docState
            if (doc != null) {
                coroutineScope.launch {
                    try {
                        repository.createDocument(doc)
                        app.onedoc.diagnostics.DiagnosticsLogger(localContext).debug("EDITOR", "Autosaved document ${doc.id} v${doc.version}")
                    } catch (e: Exception) {
                        app.onedoc.diagnostics.DiagnosticsLogger(localContext).error("EDITOR", "Autosave failed", e)
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Top bar ---
        app.onedoc.ui.components.AppTopBar(
            title = docState?.title ?: "Untitled",
            onMenuClick = { },
            onSearchClick = { },
            onMoreClick = { },
            modifier = Modifier,
        )

        // --- Formatting toolbar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { /* bold */ }) {
                Icon(imageVector = Icons.Default.FormatBold, contentDescription = "Bold")
            }
            IconButton(onClick = { /* italic */ }) {
                Icon(imageVector = Icons.Default.FormatItalic, contentDescription = "Italic")
            }
            IconButton(onClick = { /* underline */ }) {
                Icon(imageVector = Icons.Default.FormatUnderlined, contentDescription = "Underline")
            }
            Divider(modifier = Modifier.height(24.dp).width(1.dp))
            IconButton(onClick = { /* align left */ }) {
                Icon(imageVector = Icons.Default.FormatAlignLeft, contentDescription = "Align left")
            }
            IconButton(onClick = { /* align center */ }) {
                Icon(imageVector = Icons.Default.FormatAlignCenter, contentDescription = "Align center")
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* undo */ }) {
                Icon(imageVector = Icons.Default.Undo, contentDescription = "Undo")
            }
            IconButton(onClick = { /* redo */ }) {
                Icon(imageVector = Icons.Default.Redo, contentDescription = "Redo")
            }
        }

        Divider()

        // --- Title field ---
        if (docState != null) {
            TextField(
                value = docState!!.title,
                onValueChange = { newTitle ->
                    docState = docState!!.copy(
                        title = newTitle,
                        updatedAt = System.currentTimeMillis(),
                        version = docState!!.version + 1,
                    )
                },
                label = { Text(text = "Title") },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }

        Divider()

        // --- Content area ---
        if (docState != null) {
            val blocks = docState!!.blocks
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
            ) {
                items(items = blocks, key = { it.id }) { block ->
                    BlockEditor(
                        block = block,
                        onTextChange = { newText ->
                            val updatedBlocks = docState!!.blocks.map { b ->
                                if (b.id == block.id) {
                                    when (b) {
                                        is Block.Paragraph -> b.copy(
                                            text = newText,
                                            updatedAt = System.currentTimeMillis(),
                                        )
                                        is Block.Heading -> b.copy(
                                            text = newText,
                                            updatedAt = System.currentTimeMillis(),
                                        )
                                        is Block.ListItem -> b.copy(
                                            text = newText,
                                            updatedAt = System.currentTimeMillis(),
                                        )
                                        else -> b
                                    }
                                } else b
                            }
                            docState = docState!!.copy(
                                blocks = updatedBlocks,
                                updatedAt = System.currentTimeMillis(),
                                version = docState!!.version + 1,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // --- Bottom action bar ---
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

@Composable
private fun BlockEditor(
    block: Block,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textState = remember(block.id) { mutableStateOf(initialText(block)) }

    // Keep local state in sync with block
    if (textState.value != initialText(block)) {
        textState.value = initialText(block)
    }

    when (block) {
        is Block.Paragraph -> {
            TextField(
                value = textState.value,
                onValueChange = { newText ->
                    textState.value = newText
                    onTextChange(newText)
                },
                modifier = modifier,
                textStyle = MaterialTheme.typography.bodyLarge,
            )
        }
        is Block.Heading -> {
            TextField(
                value = textState.value,
                onValueChange = { newText ->
                    textState.value = newText
                    onTextChange(newText)
                },
                modifier = modifier,
                textStyle = when (block.level) {
                    1 -> MaterialTheme.typography.headlineLarge
                    2 -> MaterialTheme.typography.headlineMedium
                    3 -> MaterialTheme.typography.headlineSmall
                    else -> MaterialTheme.typography.titleLarge
                },
            )
        }
        is Block.ListItem -> {
            val prefix = if (block.ordered) "1. " else "•  "
            TextField(
                value = textState.value,
                onValueChange = { newText ->
                    val clean = newText.removePrefix(prefix)
                    textState.value = newText
                    onTextChange(clean)
                },
                modifier = modifier,
                textStyle = MaterialTheme.typography.bodyLarge,
            )
        }
        is Block.CodeBlock -> {
            val codeState = remember(block.id) { mutableStateOf(block.code) }
            TextField(
                value = codeState.value,
                onValueChange = { newText ->
                    codeState.value = newText
                    onTextChange(newText)
                },
                modifier = modifier,
                textStyle = MaterialTheme.typography.bodyMedium,
            )
        }
        is Block.Blockquote -> {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = modifier,
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                ) {
                    block.blocks.forEach { innerBlock ->
                        when (innerBlock) {
                            is Block.Paragraph -> Text(
                                text = innerBlock.text,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            else -> Text(text = innerBlock.kind.name)
                        }
                    }
                }
            }
        }
        else -> {
            Text(text = block.kind.name, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun initialText(block: Block): String = when (block) {
    is Block.Paragraph -> block.text
    is Block.Heading -> block.text
    is Block.ListItem -> block.text
    is Block.CodeBlock -> block.code
    else -> ""
}

private fun blockId() = java.util.UUID.randomUUID().toString()