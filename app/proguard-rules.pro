# OneDoc ProGuard rules (§25 release)

# Keep Compose
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }
-keepnames class androidx.compose.runtime.** { *; }

# Keep Room entities and DAOs
-keep class app.onedoc.data.** { *; }
-keep class app.onedoc.model.** { *; }

# Keep JSON library
-keep class org.json.** { *; }

# Keep model classes used by reflection (Koin, Room)
-keepnames class app.onedoc.model.Document { *; }
-keepnames class app.onedoc.model.Block { *; }
-keepnames class app.onedoc.model.Mark { *; }

# Keep ViewModel, Activities
-keep class * extends androidx.appcompat.app.AppCompatActivity
-keep class * extends androidx.activity.ComponentActivity
