package app.onedoc.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import app.onedoc.ui.theme.OneDocTheme

/**
 * Home screen — library empty-state shell (G1 T-G1-004).
 *
 * Full implementation (library list, trash, versions, stars, search) lands in G2-G4.
 * This stub renders the empty state and exposes the `od_home_empty` accessibility id
 * for direct-APK QA driving (§24).
 */
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneDocTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = "od_home_empty" }
                ) {
                    Text(
                        text = "OneDoc",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        }
    }
}
