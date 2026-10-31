package app.onedoc.model

/**
 * A OneDoc document.
 *
 * The canonical in-memory representation. Persisted to Room as [DocumentEntity]
 * and to disk as a .odoc container (JSON).
 *
 * Laws L1-L10 (§13 of the blueprint) govern how this model mutates:
 *  - L3: keystroke loss impossible under process kill (journal + snapshots)
 *  - L10: no silent mutation — every change is visible, undoable, versioned
 */
data class Document(
    val id: String,
    val title: String,
    val blocks: List<Block>,
    val version: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val trashed: Boolean = false,
    val starred: Boolean = false,
) {
    fun withTitle(title: String) = copy(title = title, updatedAt = System.currentTimeMillis(), version = version + 1)
    fun withBlocks(blocks: List<Block>) = copy(blocks = blocks, updatedAt = System.currentTimeMillis(), version = version + 1)
    fun withTrashed(trashed: Boolean) = copy(trashed = trashed, updatedAt = System.currentTimeMillis(), version = version + 1)
    fun withStarred(starred: Boolean) = copy(starred = starred, updatedAt = System.currentTimeMillis(), version = version + 1)
}
