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
        // Wait for app to initialize
        Thread.sleep(2000)
    }

    @Test
    fun appLaunchesAndNewDocumentButtonNavigatesToEditor() {
        // Step 1: Verify app launches and shows home screen
        val appTitle = device.wait(Until.findObject(By.text("OneDoc")), 5000)
        assert(appTitle != null) { "App title 'OneDoc' should be visible on launch" }

        // Step 2: Wait for "New document" button to appear
        val newDocButton = device.wait(Until.findObject(By.text("New document")), 5000)
        assert(newDocButton != null) { "New document button should be visible" }

        // Step 3: Click "New document"
        newDocButton.click()
        Thread.sleep(1500) // Wait for navigation

        // Step 4: Wait for editor to load - look for "Untitled document" title
        val titleField = device.wait(Until.findObject(By.text("Untitled document")), 5000)
        assert(titleField != null) { "Editor screen should open after clicking New document" }
    }

    @Test
    fun appLaunchesWithoutCrash() {
        // Simply verify the app launches and shows the home screen
        val appTitle = device.wait(Until.findObject(By.text("OneDoc")), 5000)
        assert(appTitle != null) { "App title 'OneDoc' should be visible on launch" }
    }
}
