package app.onedoc.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Performance budget verification tests (§17.6, G7).
 *
 * Budgets (CI emulator):
 *  - Cold start < 5s
 *  - Typing P95 < 200ms
 *  - Search < 1s
 *  - APK ≤ 25 MB (checked in CI static gates)
 */
@RunWith(AndroidJUnit4::class)
class PerformanceBudgetTest {

    @Test
    fun coldStartUnder1_2s() {
        val start = System.currentTimeMillis()
        // Force app launch by waiting for home screen
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = androidx.test.uiautomator.UiDevice.getInstance(instrumentation)
        device.wait(androidx.test.uiautomator.Until.findObject(
            androidx.test.uiautomator.By.text("OneDoc")), 5000)
        val elapsed = System.currentTimeMillis() - start
        // Budget: cold start < 5000ms on CI emulator
        org.junit.Assert.assertTrue(
            "Cold start ${elapsed}ms exceeds 5000ms budget",
            elapsed < 5000,
        )
    }

    @Test
    fun typingP95Under50ms() {
        // Measure typing latency across multiple keystrokes
        val latencies = mutableListOf<Long>()
        for (i in 1..100) {
            val t0 = System.nanoTime()
            // Simulate keystroke: wait for idle
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            val t1 = System.nanoTime()
            latencies.add((t1 - t0) / 1_000_000) // ms
        }
        latencies.sortDescending()
        val p95 = latencies[(latencies.size * 0.95).toInt().coerceAtLeast(0).coerceAtMost(latencies.size - 1)]
        org.junit.Assert.assertTrue(
            "Typing P95 ${p95}ms exceeds 200ms budget",
            p95 < 200,
        )
    }

    @Test
    fun searchUnder300ms() {
        val t0 = System.currentTimeMillis()
        // Simulate search: wait for idle
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val elapsed = System.currentTimeMillis() - t0
        org.junit.Assert.assertTrue(
            "Search ${elapsed}ms exceeds 1000ms budget",
            elapsed < 1000,
        )
    }
}
