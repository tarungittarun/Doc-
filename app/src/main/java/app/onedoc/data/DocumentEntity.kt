package app.onedoc.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import app.onedoc.model.Document

@Entity(
    tableName = "documents",
    indices = [
        Index(value = ["updated_at"], name = "idx_documents_updatedAt"),
        Index(value = ["trashed"], name = "idx_documents_trashed"),
        Index(value = ["starred"], name = "idx_documents_starred"),
    ]
)
data class DocumentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "blocks_json") val blocksJson: String,
    @ColumnInfo(name = "version") val version: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "trashed") val trashed: Boolean = false,
    @ColumnInfo(name = "starred") val starred: Boolean = false,
) {
    fun toDomain(): Document =
        Document(
            id = id,
            title = title,
            blocks = app.onedoc.model.Json.blocksFromJson(blocksJson),
            version = version,
            createdAt = createdAt,
            updatedAt = updatedAt,
            trashed = trashed,
            starred = starred,
        )

    companion object {
        fun fromDomain(doc: Document): DocumentEntity =
            DocumentEntity(
                id = doc.id,
                title = doc.title,
                blocksJson = app.onedoc.model.Json.blocksToJson(doc.blocks),
                version = doc.version,
                createdAt = doc.createdAt,
                updatedAt = doc.updatedAt,
                trashed = doc.trashed,
                starred = doc.starred,
            )
    }
}
