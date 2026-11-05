package app.onedoc

import android.content.Context
import androidx.startup.Initializer

object AppInitializer : Initializer<AppContainer> {
    override fun create(context: Context): AppContainer {
        return AppContainer(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

class AppContainer internal constructor(private val context: Context) {
}

internal fun Context.applicationContext(): Context = this.applicationContext
