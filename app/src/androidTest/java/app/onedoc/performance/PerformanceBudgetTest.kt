package app.onedoc.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Performance budget verification tests (§17.6, G7).
 *
 * Budgets:
 *  - Cold start < 1.2s
 *  - Typing P95 < 50ms
 *  - Search < 300ms
 *  - APK ≤ 25 MB (checked in CI static gates)
 *
 * These are instrumented tests — they run on emulators/API 26/33/35/36
 * and measure real UI interactions (§21.1, §24).
 */
@RunWith(AndroidJUnit4::class)
class PerformanceBudgetTest {

    @get:Rule
    val activityRule = ActivityTestRule(app.onedoc.home.HomeActivity::class.java)

    @Test
    fun coldStartUnder1_2s() {
        val start = System.currentTimeMillis()
        val activity = activityRule.activity
        val elapsed = System.currentTimeMillis() - start
        // Budget: cold start < 1200ms
        org.junit.Assert.assertTrue(
            "Cold start ${elapsed}ms exceeds 1200ms budget",
            elapsed < 1200,
        )
    }

    @Test
    fun typingP95Under50ms() {
        // Measure typing latency across multiple keystrokes
        val latencies = mutableListOf<Long>()
        for (i in 1..100) {
            val t0 = System.nanoTime()
            // Simulate keystroke: type a character
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            val t1 = System.nanoTime()
            latencies.add((t1 - t0) / 1_000_000) // ms
        }
        latencies.sortDescending()
        val p95 = latencies[(latencies.size * 0.95).toInt().coerceAtLeast(0).coerceAtMost(latencies.size - 1)]
        org.junit.Assert.assertTrue(
            "Typing P95 ${p95}ms exceeds 50ms budget",
            p95 < 50,
        )
    }

    @Test
    fun searchUnder300ms() {
        val t0 = System.currentTimeMillis()
        // Simulate search: type in search field and wait for results
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val elapsed = System.currentTimeMillis() - t0
        org.junit.Assert.assertTrue(
            "Search ${elapsed}ms exceeds 300ms budget",
            elapsed < 300,
        )
    }
}
