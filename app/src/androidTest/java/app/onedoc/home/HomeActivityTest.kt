package app.onedoc.home

import android.graphics.Bitmap
import android.os.Environment
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Instrumentation test that verifies the app launches and shows the home screen.
 * Runs on emulator in CI. Captures screenshots at each step for documentation.
 */
@RunWith(AndroidJUnit4::class)
class HomeActivityTest {

    private lateinit var device: UiDevice

    @get:Rule
    val activityRule = ActivityScenarioRule(HomeActivity::class.java)

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    private fun captureScreenshot(name: String) {
        try {
            val bitmap: Bitmap? = device.takeScreenshot()
            if (bitmap == null) return
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val extDir = context.getExternalFilesDir(null)
            val dir = if (extDir != null) File(extDir, "screenshots") else File(context.filesDir, "screenshots")
            dir.mkdirs()
            val file = File(dir, "$name.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
        } catch (e: Exception) {
            // Screenshot capture is best-effort — don't fail tests
        }
    }

    @Test
    fun appLaunchesAndShowsHomeScreen() {
        captureScreenshot("01_app_launched")

        // Wait for app to launch — check for app title OR "New document" button
        val appTitle = device.wait(Until.findObject(By.text("OneDoc")), 15000)
        assert(appTitle != null) { "App title 'OneDoc' should be visible on launch" }
        captureScreenshot("02_home_screen_visible")

        // Wait for "New document" button to appear
        val newDocButton = device.wait(Until.findObject(By.text("New document")), 15000)
        assert(newDocButton != null) { "New document button should be visible" }
        captureScreenshot("03_new_doc_button_visible")

        // Click "New document"
        newDocButton.click()
        captureScreenshot("04_after_click_new_doc")

        // Wait for editor screen - look for "Untitled" title
        val editorTitle = device.wait(Until.findObject(By.text("Untitled")), 15000)
        assert(editorTitle != null) { "Editor screen should open after clicking New document" }
        captureScreenshot("05_editor_opened")
    }

    @Test
    fun appLaunchesWithoutCrash() {
        captureScreenshot("06_launch_test_start")
        val appTitle = device.wait(Until.findObject(By.text("OneDoc")), 15000)
        assert(appTitle != null) { "App title 'OneDoc' should be visible on launch" }
        captureScreenshot("07_launch_test_done")
    }
}
