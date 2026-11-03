package app.onedoc.home

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
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

    @get:Rule
    val activityRule = ActivityScenarioRule(HomeActivity::class.java)

    @Before
    fun setUp() {
        // Wait for app to initialize
        Thread.sleep(2000)
    }

    @Test
    fun appLaunchesAndNewDocumentButtonNavigatesToEditor() {
        val device = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation

        // Wait for "New document" button to appear
        val newDocButton = device.wait(Until.findObject(By.text("New document")), 5000)
        assert(newDocButton != null) { "New document button should be visible" }

        // Click "New document"
        newDocButton.click()

        // Wait for editor to load - look for title TextField
        val titleField = device.wait(Until.findObject(By.res("android:id/text1")), 5000)
        assert(titleField != null) { "Editor screen should open after clicking New document" }
    }

    @Test
    fun appLaunchesWithoutCrash() {
        // Simply verify the app launches and shows the home screen
        val device = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation
        val appTitle = device.wait(Until.findObject(By.text("OneDoc")), 5000)
        assert(appTitle != null) { "App title 'OneDoc' should be visible on launch" }
    }
}
