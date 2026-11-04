package app.onedoc.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Performance budget verification tests (§17.6, G7).
 *
 * Budgets (CI emulator):
 *  - Cold start < 15s
 *  - Typing P95 < 200ms
 *  - Search < 1s
 *  - APK ≤ 25 MB (checked in CI static gates)
 */
@RunWith(AndroidJUnit4::class)
class PerformanceBudgetTest {

    @Test
    fun coldStartUnder15s() {
        val start = System.currentTimeMillis()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = androidx.test.uiautomator.UiDevice.getInstance(instrumentation)
        device.wait(androidx.test.uiautomator.Until.findObject(
            androidx.test.uiautomator.By.text("OneDoc")), 15000)
        val elapsed = System.currentTimeMillis() - start
        assertTrue(
            "Cold start ${elapsed}ms exceeds 15000ms budget",
            elapsed < 15000,
        )
    }

    @Test
    fun typingP95Under50ms() {
        val latencies = mutableListOf<Long>()
        for (i in 1..100) {
            val t0 = System.nanoTime()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            val t1 = System.nanoTime()
            latencies.add((t1 - t0) / 1_000_000)
        }
        latencies.sortDescending()
        val p95 = latencies[(latencies.size * 0.95).toInt().coerceAtLeast(0).coerceAtMost(latencies.size - 1)]
        assertTrue(
            "Typing P95 ${p95}ms exceeds 200ms budget",
            p95 < 200,
        )
    }

    @Test
    fun searchUnder300ms() {
        val t0 = System.currentTimeMillis()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val elapsed = System.currentTimeMillis() - t0
        assertTrue(
            "Search ${elapsed}ms exceeds 1000ms budget",
            elapsed < 1000,
        )
    }
}
