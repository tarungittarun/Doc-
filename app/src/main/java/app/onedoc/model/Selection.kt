package app.onedoc.model

data class Selection(
    val blockId: String,
    val start: Int,
    val end: Int,
) {
    fun isCollapsed() = start == end
    fun length() = end - start
}
