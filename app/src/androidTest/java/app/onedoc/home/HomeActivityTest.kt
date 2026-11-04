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
 * Instrumentation test that verifies the app launches and shows the home screen.
 * Runs on emulator in CI.
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

    @Test
    fun appLaunchesAndShowsHomeScreen() {
        // Wait for app to launch and show home screen
        val appTitle = device.wait(Until.findObject(By.text("OneDoc")), 15000)
        assert(appTitle != null) { "App title 'OneDoc' should be visible on launch" }

        // Wait for "New document" button to appear
        val newDocButton = device.wait(Until.findObject(By.text("New document")), 15000)
        assert(newDocButton != null) { "New document button should be visible" }

        // Click "New document"
        newDocButton.click()

        // Wait for editor screen - look for "Untitled" title (fallback shown before doc loads)
        val editorTitle = device.wait(Until.findObject(By.text("Untitled")), 15000)
        assert(editorTitle != null) { "Editor screen should open after clicking New document" }
    }

    @Test
    fun appLaunchesWithoutCrash() {
        val appTitle = device.wait(Until.findObject(By.text("OneDoc")), 15000)
        assert(appTitle != null) { "App title 'OneDoc' should be visible on launch" }
    }
}
