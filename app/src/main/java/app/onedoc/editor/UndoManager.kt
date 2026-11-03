package app.onedoc.editor

import app.onedoc.model.Document

class UndoManager(private val maxHistory: Int = 100) {
    private var undoStack: List<Document> = emptyList()
    private var redoStack: List<Document> = emptyList()

    var current: Document = Document(
        id = "",
        title = "",
        blocks = emptyList(),
        version = 0,
        createdAt = 0,
        updatedAt = 0,
    )
        private set

    fun push(next: Document) {
        undoStack = (undoStack + current).takeLast(maxHistory)
        redoStack = emptyList()
        current = next
    }

    fun undo(): Document {
        if (undoStack.isEmpty()) return current
        redoStack = redoStack + current
        val previous = undoStack.last()
        undoStack = undoStack.dropLast(1)
        current = previous
        return current
    }

    fun redo(): Document {
        if (redoStack.isEmpty()) return current
        undoStack = undoStack + current
        val next = redoStack.last()
        redoStack = redoStack.dropLast(1)
        current = next
        return current
    }

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun record(doc: Document) {
        push(doc)
    }
}
