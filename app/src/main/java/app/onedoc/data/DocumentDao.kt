package app.onedoc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import app.onedoc.model.Document
import java.util.*

@Dao
interface DocumentDao {

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insert(document: DocumentEntity)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg documents: DocumentEntity)

    @Update
    suspend fun update(document: DocumentEntity)

    @Delete
    suspend fun delete(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun findById(id: String): DocumentEntity?

    @Query("""
        SELECT * FROM documents
        WHERE trashed = 0
        ORDER BY updatedAt DESC
    """)
    suspend fun getAllActive(): List<DocumentEntity>

    @Query("""
        SELECT * FROM documents
        WHERE trashed = 1
        ORDER BY updatedAt DESC
    """)
    suspend fun getTrashed(): List<DocumentEntity>

    @Query("""
        SELECT * FROM documents
        WHERE starred = 1 AND trashed = 0
        ORDER BY updatedAt DESC
    """)
    suspend fun getStarred(): List<DocumentEntity>

    @Query("""
        SELECT * FROM documents
        WHERE (LOWER(title) LIKE LOWER('%' || :query || '%'))
        ORDER BY updatedAt DESC
    """)
    suspend fun search(query: String): List<DocumentEntity>

    @Query("UPDATE documents SET updated_at = :updatedAt, version = :version WHERE id = :id")
    suspend fun touch(id: String, updatedAt: Long, version: Long)
}
