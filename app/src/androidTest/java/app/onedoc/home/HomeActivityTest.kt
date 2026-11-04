package app.onedoc.home

import android.graphics.Bitmap
import android.os.Environment
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test that verifies the app launches and the "New document"
 * button navigates to the editor screen. Runs on emulator in CI.
 * Captures screenshots at each step for PDF documentation.
 */
@RunWith(AndroidJUnit4::class)
class HomeActivityTest {

    private lateinit var device: UiDevice
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    @get:Rule
    val activityRule = ActivityScenarioRule(HomeActivity::class.java)

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // Wait for app to initialize
        Thread.sleep(2000)
    }

    private fun captureScreenshot(name: String) {
        try {
            val bitmap: Bitmap? = device.takeScreenshot()
            if (bitmap != null) {
                val timestamp = dateFormat.format(Date())
                val fileName = "onedoc_${name}_$timestamp.png"
                val screenshotsDir = File(
                    InstrumentationRegistry.getInstrumentation().targetContext
                        .getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "onedoc_screenshots"
                )
                screenshotsDir.mkdirs()
                val file = File(screenshotsDir, fileName)
                file.outputStream().use { os ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, os)
                }
            }
        } catch (e: Exception) {
            // Screenshot capture failure should not fail the test
        }
    }

    @Test
    fun appLaunchesAndNewDocumentButtonNavigatesToEditor() {
        // Step 1: Verify app launches and shows home screen
        captureScreenshot("01_home_launch")
        val appTitle = device.wait(Until.findObject(By.text("OneDoc")), 5000)
        assert(appTitle != null) { "App title 'OneDoc' should be visible on launch" }

        // Step 2: Wait for "New document" button to appear
        captureScreenshot("02_home_with_button")
        val newDocButton = device.wait(Until.findObject(By.text("New document")), 5000)
        assert(newDocButton != null) { "New document button should be visible" }

        // Step 3: Click "New document"
        newDocButton.click()
        Thread.sleep(1500) // Wait for navigation
        captureScreenshot("03_after_click_new_document")

        // Step 4: Wait for editor to load - look for "Untitled document" title
        val titleField = device.wait(Until.findObject(By.text("Untitled document")), 5000)
        assert(titleField != null) { "Editor screen should open after clicking New document" }

        captureScreenshot("04_editor_loaded")
    }

    @Test
    fun appLaunchesWithoutCrash() {
        // Simply verify the app launches and shows the home screen
        captureScreenshot("crash_test_home")
        val appTitle = device.wait(Until.findObject(By.text("OneDoc")), 5000)
        assert(appTitle != null) { "App title 'OneDoc' should be visible on launch" }
        captureScreenshot("crash_test_passed")
    }
}
