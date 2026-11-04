package app.onedoc.home

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

/**
 * Instrumentation test that verifies the app launches and the "New document"
 * button navigates to the editor screen. Runs on emulator in CI.
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
            val bitmap = device.takeScreenshot() ?: return
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val dir = File(context.getExternalFilesDir(null), "screenshots")
            dir.mkdirs()
            val file = File(dir, "$name.png")
            file.outputStream().use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, out)
            }
        } catch (e: Exception) {
            // Screenshot capture is best-effort
        }
    }

    @Test
    fun appLaunchesAndNewDocumentButtonNavigatesToEditor() {
        captureScreenshot("01_app_launched")
        val appTitle = device.wait(Until.findObject(By.text("OneDoc")), 10000)
        assert(appTitle != null) { "App title 'OneDoc' should be visible on launch" }

        captureScreenshot("02_home_screen")
        val newDocButton = device.wait(Until.findObject(By.text("New document")), 10000)
        assert(newDocButton != null) { "New document button should be visible" }

        newDocButton.click()

        captureScreenshot("03_after_click_new_doc")
        val titleField = device.wait(Until.findObject(By.text("Untitled document")), 10000)
        assert(titleField != null) { "Editor screen should open after clicking New document" }
        captureScreenshot("04_editor_opened")
    }

    @Test
    fun appLaunchesWithoutCrash() {
        captureScreenshot("05_launch_test")
        val appTitle = device.wait(Until.findObject(By.text("OneDoc")), 10000)
        assert(appTitle != null) { "App title 'OneDoc' should be visible on launch" }
        captureScreenshot("06_launch_test_done")
    }
}
