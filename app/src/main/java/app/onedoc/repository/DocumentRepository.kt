package app.onedoc.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import app.onedoc.data.AppDatabase
import app.onedoc.data.DocumentDao
import app.onedoc.data.DocumentEntity
import app.onedoc.model.Document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DocumentRepository(private val database: AppDatabase, private val scope: CoroutineScope) {
    private val dao: DocumentDao = database.documentDao()

    private val _documents = MutableLiveData<List<Document>>(emptyList())
    val documents: LiveData<List<Document>> = _documents

    init {
        scope.launch { refresh() }
    }

    suspend fun refresh() {
        val entities = dao.getAllActive()
        val docs = entities.map { it.toDomain() }
        scope.launch {
            _documents.value = docs
        }
    }

    suspend fun getDocument(id: String): Document? {
        return dao.findById(id)?.toDomain()
    }

    suspend fun createDocument(doc: Document) = dao.insert(DocumentEntity.fromDomain(doc))

    suspend fun saveDocument(doc: Document) {
        dao.insert(DocumentEntity.fromDomain(doc))
        refresh()
    }

    suspend fun deleteDocument(id: String) = dao.deleteById(id)

    suspend fun trashDocument(id: String) {
        val entity = dao.findById(id) ?: return
        dao.update(entity.copy(trashed = true, updatedAt = System.currentTimeMillis()))
        refresh()
    }

    suspend fun restoreDocument(id: String) {
        val entity = dao.findById(id) ?: return
        dao.update(entity.copy(trashed = false, updatedAt = System.currentTimeMillis()))
        refresh()
    }

    suspend fun starDocument(id: String, starred: Boolean) {
        val entity = dao.findById(id) ?: return
        dao.update(entity.copy(starred = starred, updatedAt = System.currentTimeMillis()))
        refresh()
    }

    suspend fun searchDocuments(query: String): List<Document> {
        return dao.search(query).map { it.toDomain() }
    }

    companion object {
        fun getInstance(context: Context): DocumentRepository {
            val database = AppDatabase.getDatabase(context)
            val scope = kotlinx.coroutines.MainScope()
            return DocumentRepository(database, scope)
        }
    }
}
