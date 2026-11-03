package app.onedoc.data

import androidx.room.TypeConverter
import app.onedoc.model.Block
import app.onedoc.model.Json

class BlocksConverter {
    @TypeConverter
    fun blocksToJson(blocks: List<Block>?): String? = blocks?.let { Json.blocksToJson(it) }

    @TypeConverter
    fun blocksFromJson(json: String?): List<Block>? = json?.let { Json.blocksFromJson(it) }
}
