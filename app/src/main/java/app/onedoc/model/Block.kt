package app.onedoc.model

/**
 * A block within a document. OneDoc supports 13 block kinds (§14):
 *
 *  1. paragraph   — body text
 *  2. heading     — H1–H6
 *  3. bulletList  — unordered list item
 *  4. orderedList — ordered list item
 *  5. codeBlock   — fenced code
 *  6. blockquote  — quoted text
 *  7. image       — embedded image
 *  8. table       — grid of cells
 *  9. toc         — table of contents
 * 10. footnote    — footnote reference
 * 11. divider     — horizontal rule
 * 12. pageBreak   — page break
 * 13. title       — document title block
 */
sealed class Block {
    abstract val id: String
    abstract val kind: BlockKind
    abstract val marks: List<Mark>
    abstract val createdAt: Long

    data class Paragraph(
        override val id: String,
        override val kind: BlockKind = BlockKind.Paragraph,
        val text: String,
        override val marks: List<Mark> = emptyList(),
        override val createdAt: Long = System.currentTimeMillis(),
    ) : Block()

    data class Heading(
        override val id: String,
        override val kind: BlockKind = BlockKind.Heading,
        val level: Int, // 1..6
        val text: String,
        override val marks: List<Mark> = emptyList(),
        override val createdAt: Long = System.currentTimeMillis(),
    ) : Block()

    data class ListItem(
        override val id: String,
        override val kind: BlockKind,
        val text: String,
        val ordered: Boolean = false,
        val level: Int = 0,
        override val marks: List<Mark> = emptyList(),
        override val createdAt: Long = System.currentTimeMillis(),
    ) : Block()

    data class CodeBlock(
        override val id: String,
        override val kind: BlockKind = BlockKind.CodeBlock,
        val code: String,
        val language: String = "",
        override val marks: List<Mark> = emptyList(),
        override val createdAt: Long = System.currentTimeMillis(),
    ) : Block()

    data class Blockquote(
        override val id: String,
        override val kind: BlockKind = BlockKind.Blockquote,
        val blocks: List<Block>,
        override val marks: List<Mark> = emptyList(),
        override val createdAt: Long = System.currentTimeMillis(),
    ) : Block()

    data class ImageBlock(
        override val id: String,
        override val kind: BlockKind = BlockKind.Image,
        val uri: String,
        val altText: String = "",
        val width: Int = 0,
        val height: Int = 0,
        override val marks: List<Mark> = emptyList(),
        override val createdAt: Long = System.currentTimeMillis(),
    ) : Block()

    data class Table(
        override val id: String,
        override val kind: BlockKind = BlockKind.Table,
        val rows: Int,
        val cols: Int,
        val cells: List<List<String>>,
        override val marks: List<Mark> = emptyList(),
        override val createdAt: Long = System.currentTimeMillis(),
    ) : Block()

    data class TOC(
        override val id: String,
        override val kind: BlockKind = BlockKind.TOC,
        val maxLevel: Int = 6,
        override val createdAt: Long = System.currentTimeMillis(),
    ) : Block()

    data class Footnote(
        override val id: String,
        override val kind: BlockKind = BlockKind.Footnote,
        val text: String,
        override val createdAt: Long = System.currentTimeMillis(),
    ) : Block()

    data class Divider(
        override val id: String,
        override val kind: BlockKind = BlockKind.Divider,
        override val createdAt: Long = System.currentTimeMillis(),
    ) : Block()

    data class PageBreak(
        override val id: String,
        override val kind: BlockKind = BlockKind.PageBreak,
        override val createdAt: Long = System.currentTimeMillis(),
    ) : Block()

    data class Title(
        override val id: String,
        override val kind: BlockKind = BlockKind.Title,
        val text: String,
        override val marks: List<Mark> = emptyList(),
        override val createdAt: Long = System.currentTimeMillis(),
    ) : Block()
}

enum class BlockKind {
    Paragraph, Heading, ListItem, CodeBlock, Blockquote, Image, Table,
    TOC, Footnote, Divider, PageBreak, Title
}

fun blockId() = java.util.UUID.randomUUID().toString()
