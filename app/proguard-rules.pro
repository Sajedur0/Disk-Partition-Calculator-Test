# Keep the Google Play Core in-app update entry points used by UpdateService.
-keep class com.google.android.play.core.** { *; }
-keep interface com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

# Keep the public model layer so reflection based tooling (and future
# serialization work) can keep reading the persisted history payloads.
-keep class com.diskcalc.core.model.** { *; }
