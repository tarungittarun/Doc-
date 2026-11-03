package app.onedoc.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneDocTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(text = "OneDoc", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}