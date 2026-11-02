package app.onedoc.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.onedoc.model.Block
import app.onedoc.model.Document
import app.onedoc.model.Json
import app.onedoc.repository.DocumentRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test that exercises the full startup path that crashed on device:
 * Room database initialization + DAO queries + DocumentRepository creation.
 *
 * This catches the Room index/column-name mismatch ("updatedAt" vs "updated_at")
 * that caused a fatal RoomSchemaException during Room.databaseBuilder().build(),
 * which manifested as "OneDoc keeps stopping" on launch.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "onedoc_test_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Test
    fun roomDatabaseInitializesWithoutCrash() {
        // Room.schema validation happens here — index/column mismatches throw fatally.
        val dao = database.documentDao()
        assertNotNull(dao)
    }

    @Test
    fun documentInsertAndQueryRoundTrip() = runBlocking {
        val dao = database.documentDao()
        val blocksJson = Json.blocksToJson(
            listOf(Block.Paragraph(id = "p1", text = "Hello"))
        )
        val entity = DocumentEntity(
            id = "doc-1",
            title = "Test Doc",
            blocksJson = blocksJson,
            version = 1,
            createdAt = 1000L,
            updatedAt = 2000L,
        )
        dao.insert(entity)

        val found = dao.findById("doc-1")
        assertNotNull(found)
        assertEquals("Test Doc", found?.title)

        val all = dao.getAllActive()
        assertEquals(1, all.size)
    }

    @Test
    fun documentRepositoryInitializesWithoutCrash() {
        // This is what HomeActivity.onCreate() does lazily — it must not throw.
        val repository = DocumentRepository.getInstance(context)
        assertNotNull(repository)
        assertNotNull(repository.documents)
    }
}
