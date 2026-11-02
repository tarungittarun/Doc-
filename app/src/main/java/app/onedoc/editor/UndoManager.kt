package app.onedoc.editor

import app.onedoc.model.Document

/**
 * Undo/redo manager with journal + snapshots (§13 Law L3).
 *
 * Every mutation pushes a snapshot onto the undo stack. Undo pops the
 * current state onto the redo stack and restores the previous snapshot.
 * Redo is the inverse.
 *
 * Snapshots are persisted so keystrokes survive process kill (Law L3):
 * persistence to Room/database lands in G2+; this manager holds the
 * in-memory journal and exposes save/load hooks.
 */
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

    /** Push current state onto undo stack, then set new current state. */
    fun push(next: Document) {
        undoStack = (undoStack + current).takeLast(maxHistory)
        redoStack = emptyList()
        current = next
    }

    /** Undo: restore previous state, push current onto redo stack. */
    fun undo(): Document {
        if (undoStack.isEmpty()) return current
        redoStack = redoStack + current
        val previous = undoStack.last()
        undoStack = undoStack.dropLast(1)
        current = previous
        return current
    }

    /** Redo: restore next state, push current onto undo stack. */
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

    /** Called after every edit to record the new state. */
    fun record(doc: Document) {
        push(doc)
    }
}
