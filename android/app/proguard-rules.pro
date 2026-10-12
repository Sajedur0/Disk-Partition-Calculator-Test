# Flutter Wrapper & Embedding rules
-keep class io.flutter.app.** { *; }
-keep class io.flutter.plugin.** { *; }
-keep class io.flutter.util.** { *; }
-keep class io.flutter.view.** { *; }
-keep class io.flutter.embedding.** { *; }
-keep class io.flutter.provider.** { *; }
-keep class io.flutter.plugins.** { *; }

# Google Play Core App Update library rules
-keep class com.google.android.play.core.** { *; }
-keep interface com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

# Local / Plugin classes for in_app_update_android
-keep class in_app_update_android.** { *; }
-keepclassmembers class in_app_update_android.** { *; }

# Generated plugin registrant
-keep class io.flutter.plugins.GeneratedPluginRegistrant { *; }

# Retain Android Lifecycle / Activity methods for Flutter plugins
-keep class * extends io.flutter.embedding.engine.plugins.FlutterPlugin { *; }
-keep class * extends io.flutter.embedding.engine.plugins.activity.ActivityAware { *; }
