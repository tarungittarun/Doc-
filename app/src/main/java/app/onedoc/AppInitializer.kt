package app.onedoc

import android.content.Context
import androidx.startup.Initializer
import io.insertkoin.android.ext.android.inject
import io.insertkoin.core.context.startKoin
import io.insertkoin.core.module.module

/**
 * OneDoc DI bootstrap.
 *
 * Koin graph skeleton. Modules are added per feature as G2-G4 land.
 * No network classes are referenced anywhere in the graph (privacy contract C1).
 */
object AppInitializer : Initializer<AppContainer> {
    override fun create(context: Context): AppContainer {
        startKoin {
            modules(listOf(appModule))
        }
        return AppContainer(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    private val appModule = module {
        // single-instance app container
        single { AppContainer(get()) }
    }
}

class AppContainer internal constructor(private val context: Context) {
    // Feature repositories, DAOs, and the journal are wired here as they land (G2+).
}

/** Minimal Context import shim kept explicit for clarity. */
internal fun Context.applicationContext(): Context = this.applicationContext
