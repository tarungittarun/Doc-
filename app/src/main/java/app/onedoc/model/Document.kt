package app.onedoc.model

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
