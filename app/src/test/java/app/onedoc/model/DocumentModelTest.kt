package app.onedoc.model

import org.json.JSONException
import org.junit.Assert.*
import org.junit.Test

class DocumentModelTest {

    @Test
    fun documentRoundTrip() {
        val doc = Document(
            id = "doc-1",
            title = "Hello World",
            blocks = listOf(
                Block.Paragraph(
                    id = "b1",
                    text = "Hello **world**",
                    marks = listOf(Mark.Bold()),
                ),
                Block.Heading(
                    id = "b2",
                    level = 1,
                    text = "Title",
                ),
                Block.ListItem(
                    id = "b3",
                    text = "Item 1",
                    ordered = true,
                ),
                Block.CodeBlock(
                    id = "b4",
                    code = "println(\"hi\")",
                    language = "kotlin",
                ),
            ),
            version = 1,
            createdAt = 1000L,
            updatedAt = 2000L,
        )

        val json = Json.blocksToJson(doc.blocks)
        val blocksBack = Json.blocksFromJson(json)

        assertEquals(doc.blocks.size, blocksBack.size)

        val p0 = blocksBack[0] as Block.Paragraph
        assertEquals("Hello **world**", p0.text)
        assertEquals(1, p0.marks.size)
        assertTrue(p0.marks[0] is Mark.Bold)

        val h0 = blocksBack[1] as Block.Heading
        assertEquals(1, h0.level)
        assertEquals("Title", h0.text)

        val l0 = blocksBack[2] as Block.ListItem
        assertTrue(l0.ordered)
        assertEquals("Item 1", l0.text)

        val c0 = blocksBack[3] as Block.CodeBlock
        assertEquals("kotlin", c0.language)
        assertEquals("println(\"hi\")", c0.code)
    }

    @Test
    fun emptyBlocksRoundTrip() {
        val json = Json.blocksToJson(emptyList())
        val blocks = Json.blocksFromJson(json)
        assertTrue(blocks.isEmpty())
    }

    @Test(expected = JSONException::class)
    fun invalidJsonThrows() {
        Json.blocksFromJson("{not valid json}")
    }

    @Test
    fun documentCopyUpdatesVersion() {
        val doc = Document(
            id = "d1",
            title = "T",
            blocks = emptyList(),
            version = 5,
            createdAt = 1L,
            updatedAt = 2L,
        )
        val updated = doc.withTitle("New Title")
        assertEquals("New Title", updated.title)
        assertEquals(6L, updated.version)
        assertNotEquals(doc.updatedAt, updated.updatedAt)
    }
}
