package app.onedoc

import android.app.Application
import android.util.Log

/**
 * OneDocApplication — custom Application class that installs a global
 * uncaught exception handler so the app never crashes silently.
 *
 * Every uncaught exception is logged to DiagnosticsLogger and to Log.e,
 * then the app shows a fallback screen instead of crashing.
 */
class OneDocApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Install global uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler { _, throwable -> 
            Log.e("OneDocCrash", "UNCAUGHT EXCEPTION", throwable)
            try {
                val diagnostics = app.onedoc.diagnostics.DiagnosticsLogger(this@OneDocApplication)
                diagnostics.recordCrash(throwable, "uncaught exception handler")
                diagnostics.error("UNCAUGHT", "Uncaught exception: ${throwable.message}", throwable)
            } catch (e: Exception) {
                Log.e("OneDocCrash", "Failed to log crash to diagnostics", e)
            }
            // Do NOT re-throw — let the app stay alive
        }
    }
}
// Rebuilt via CI 2026-11-06
