# Keep kotlinx.serialization generated serializers for the history DTOs.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class com.diskcalc.data.dto.** {
    *** Companion;
}
-keepclasseswithmembers class com.diskcalc.data.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.diskcalc.data.dto.**$$serializer { *; }

# Play Core in-app update is optional at runtime.
-dontwarn com.google.android.play.core.**
