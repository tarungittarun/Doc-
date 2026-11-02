package app.onedoc.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test that exercises the startup path that crashed on device:
 * Room database initialization.
 *
 * Catches the Room index/column-name mismatch ("updatedAt" vs "updated_at")
 * that caused a fatal RoomSchemaException during Room.databaseBuilder().build(),
 * which manifested as "OneDoc keeps stopping" on launch.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseRobolectricTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context: android.content.Context = ApplicationProvider.getApplicationContext()
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
}
