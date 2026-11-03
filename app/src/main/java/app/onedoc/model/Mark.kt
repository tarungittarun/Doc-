package app.onedoc.model

sealed class Mark {
    abstract val type: MarkType
    data class Bold(override val type: MarkType = MarkType.Bold) : Mark()
    data class Italic(override val type: MarkType = MarkType.Italic) : Mark()
    data class Underline(override val type: MarkType = MarkType.Underline) : Mark()
    data class Strikethrough(override val type: MarkType = MarkType.Strikethrough) : Mark()
    data class Link(val url: String, override val type: MarkType = MarkType.Link) : Mark()
    data class Code(override val type: MarkType = MarkType.Code) : Mark()
    data class Highlight(val color: String = "#FFFF00", override val type: MarkType = MarkType.Highlight) : Mark()
}

enum class MarkType { Bold, Italic, Underline, Strikethrough, Link, Code, Highlight }
