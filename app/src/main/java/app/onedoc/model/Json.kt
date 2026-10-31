package app.onedoc.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * JSON serialization for blocks and marks (§14.2 JSON schema, Appendix AN).
 * Used by DocumentEntity for Room persistence and by .odoc import/export (§18).
 */
object Json {
    fun blocksToJson(blocks: List<Block>): String {
        val arr = JSONArray()
        blocks.forEach { arr.put(blockToJson(it)) }
        return arr.toString()
    }

    fun blocksFromJson(json: String): List<Block> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { blockFromJson(arr.getJSONObject(it)) }
    }

    private fun blockToJson(block: Block): JSONObject {
        val obj = JSONObject()
        obj.put("id", block.id)
        obj.put("kind", block.kind.name)
        obj.put("createdAt", block.createdAt)
        return when (block) {
            is Block.Paragraph -> obj.apply {
                obj.put("type", "paragraph")
                obj.put("text", block.text)
                obj.put("marks", marksToJson(block.marks))
            }
            is Block.Heading -> obj.apply {
                obj.put("type", "heading")
                obj.put("level", block.level)
                obj.put("text", block.text)
                obj.put("marks", marksToJson(block.marks))
            }
            is Block.ListItem -> obj.apply {
                obj.put("type", if (block.ordered) "orderedList" else "bulletList")
                obj.put("text", block.text)
                obj.put("level", block.level)
                obj.put("marks", marksToJson(block.marks))
            }
            is Block.CodeBlock -> obj.apply {
                obj.put("type", "codeBlock")
                obj.put("code", block.code)
                obj.put("language", block.language)
            }
            is Block.Blockquote -> obj.apply {
                obj.put("type", "blockquote")
                obj.put("blocks", blocksToJson(block.blocks))
            }
            is Block.ImageBlock -> obj.apply {
                obj.put("type", "image")
                obj.put("uri", block.uri)
                obj.put("altText", block.altText)
                obj.put("width", block.width)
                obj.put("height", block.height)
            }
            is Block.Table -> obj.apply {
                obj.put("type", "table")
                obj.put("rows", block.rows)
                obj.put("cols", block.cols)
                val cellsArr = JSONArray()
                block.cells.forEach { row ->
                    val rowArr = JSONArray()
                    row.forEach { rowArr.put(it) }
                    cellsArr.put(rowArr)
                }
                obj.put("cells", cellsArr)
            }
            is Block.TOC -> obj.apply {
                obj.put("type", "toc")
                obj.put("maxLevel", block.maxLevel)
            }
            is Block.Footnote -> obj.apply {
                obj.put("type", "footnote")
                obj.put("text", block.text)
            }
            is Block.Divider -> obj.put("type", "divider")
            is Block.PageBreak -> obj.put("type", "pageBreak")
            is Block.Title -> obj.apply {
                obj.put("type", "title")
                obj.put("text", block.text)
                obj.put("marks", marksToJson(block.marks))
            }
        }
    }

    private fun blockFromJson(obj: JSONObject): Block {
        val id = obj.getString("id")
        val kind = BlockKind.valueOf(obj.getString("kind"))
        val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        return when (obj.getString("type")) {
            "paragraph" -> Block.Paragraph(
                id = id,
                text = obj.getString("text"),
                marks = marksFromJson(obj.optJSONArray("marks")),
                createdAt = createdAt,
            )
            "heading" -> Block.Heading(
                id = id,
                level = obj.getInt("level"),
                text = obj.getString("text"),
                marks = marksFromJson(obj.optJSONArray("marks")),
                createdAt = createdAt,
            )
            "bulletList" -> Block.ListItem(
                id = id,
                kind = BlockKind.ListItem,
                text = obj.getString("text"),
                ordered = false,
                level = obj.optInt("level", 0),
                marks = marksFromJson(obj.optJSONArray("marks")),
                createdAt = createdAt,
            )
            "orderedList" -> Block.ListItem(
                id = id,
                kind = BlockKind.ListItem,
                text = obj.getString("text"),
                ordered = true,
                level = obj.optInt("level", 0),
                marks = marksFromJson(obj.optJSONArray("marks")),
                createdAt = createdAt,
            )
            "codeBlock" -> Block.CodeBlock(
                id = id,
                code = obj.getString("code"),
                language = obj.optString("language", ""),
                createdAt = createdAt,
            )
            "blockquote" -> Block.Blockquote(
                id = id,
                blocks = blocksFromJson(obj.getString("blocks")),
                createdAt = createdAt,
            )
            "image" -> Block.ImageBlock(
                id = id,
                uri = obj.getString("uri"),
                altText = obj.optString("altText", ""),
                width = obj.optInt("width", 0),
                height = obj.optInt("height", 0),
                createdAt = createdAt,
            )
            "table" -> obj.apply {
                val cellsArr = obj.getJSONArray("cells")
                val cells = (0 until cellsArr.length()).map { i ->
                    val rowArr = cellsArr.getJSONArray(i)
                    (0 until rowArr.length()).map { j -> rowArr.getString(j) }
                }
                return Block.Table(
                    id = id,
                    rows = obj.getInt("rows"),
                    cols = obj.getInt("cols"),
                    cells = cells,
                    createdAt = createdAt,
                )
            }
            "toc" -> Block.TOC(
                id = id,
                maxLevel = obj.optInt("maxLevel", 6),
                createdAt = createdAt,
            )
            "footnote" -> Block.Footnote(
                id = id,
                text = obj.getString("text"),
                createdAt = createdAt,
            )
            "divider" -> Block.Divider(id = id, createdAt = createdAt)
            "pageBreak" -> Block.PageBreak(id = id, createdAt = createdAt)
            "title" -> Block.Title(
                id = id,
                text = obj.getString("text"),
                marks = marksFromJson(obj.optJSONArray("marks")),
                createdAt = createdAt,
            )
            else -> throw IllegalArgumentException("Unknown block type: ${obj.getString("type")}")
        }
    }

    private fun marksToJson(marks: List<Mark>): JSONArray {
        val arr = JSONArray()
        marks.forEach { arr.put(markToJson(it)) }
        return arr
    }

    private fun markToJson(mark: Mark): JSONObject {
        val obj = JSONObject()
        obj.put("type", mark.type.name)
        return when (mark) {
            is Mark.Link -> obj.put("url", mark.url)
            is Mark.Highlight -> obj.put("color", mark.color)
            else -> obj
        }
    }

    private fun marksFromJson(arr: JSONArray?): List<Mark> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { markFromJson(arr.getJSONObject(it)) }
    }

    private fun markFromJson(obj: JSONObject): Mark? {
        return when (MarkType.valueOf(obj.getString("type"))) {
            MarkType.Bold -> Mark.Bold()
            MarkType.Italic -> Mark.Italic()
            MarkType.Underline -> Mark.Underline()
            MarkType.Strikethrough -> Mark.Strikethrough()
            MarkType.Link -> Mark.Link(url = obj.optString("url", ""))
            MarkType.Code -> Mark.Code()
            MarkType.Highlight -> Mark.Highlight(color = obj.optString("color", "#FFFF00"))
        }
    }
}
