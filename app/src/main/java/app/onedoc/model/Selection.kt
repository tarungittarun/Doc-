package app.onedoc.model

/**
 * A text selection within a document: which block, and the character range [start, end).
 */
data class Selection(
    val blockId: String,
    val start: Int,
    val end: Int,
) {
    fun isCollapsed() = start == end
    fun length() = end - start
}
