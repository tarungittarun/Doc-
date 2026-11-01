package app.onedoc.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * OneDoc settings persisted via DataStore (§17.7).
 *
 * Keys:
 *  - darkTheme: user prefers dark theme
 *  - fontSizeScale: 1=Small, 2=Medium, 3=Large
 *  - reducedMotion: minimize animations
 *  - locale: language code (en, hi, mr)
 */
data class AppSettings(
    val darkTheme: Boolean = false,
    val fontSizeScale: Int = 2, // 1=Small, 2=Medium, 3=Large
    val reducedMotion: Boolean = false,
    val locale: String = "en",
)

class SettingsDataStore(private val dataStore: DataStore<Preferences>) {

    private val DARK_THEME = booleanPreferencesKey("dark_theme")
    private val FONT_SIZE_SCALE = intPreferencesKey("font_size_scale")
    private val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
    private val LOCALE = stringPreferencesKey("locale")

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            darkTheme = prefs[DARK_THEME] ?: false,
            fontSizeScale = prefs[FONT_SIZE_SCALE] ?: 2,
            reducedMotion = prefs[REDUCED_MOTION] ?: false,
            locale = prefs[LOCALE] ?: "en",
        )
    }

    suspend fun updateSettings(update: AppSettings.() -> AppSettings) {
        dataStore.edit { prefs ->
            val current = AppSettings(
                darkTheme = prefs[DARK_THEME] ?: false,
                fontSizeScale = prefs[FONT_SIZE_SCALE] ?: 2,
                reducedMotion = prefs[REDUCED_MOTION] ?: false,
                locale = prefs[LOCALE] ?: "en",
            )
            val next = current.update()
            prefs[DARK_THEME] = next.darkTheme
            prefs[FONT_SIZE_SCALE] = next.fontSizeScale
            prefs[REDUCED_MOTION] = next.reducedMotion
            prefs[LOCALE] = next.locale
        }
    }

    suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { prefs ->
            prefs[DARK_THEME] = settings.darkTheme
            prefs[FONT_SIZE_SCALE] = settings.fontSizeScale
            prefs[REDUCED_MOTION] = settings.reducedMotion
            prefs[LOCALE] = settings.locale
        }
    }
}

class SettingsDataStoreFactory {
    fun create(context: Context): SettingsDataStore {
        val dataStore = androidx.datastore.preferences.preferencesDataStore(context, "onedoc_settings")
        return SettingsDataStore(dataStore)
    }
}
