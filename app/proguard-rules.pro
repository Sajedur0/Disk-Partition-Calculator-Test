# Keep kotlinx.serialization generated serializers for the history DTOs.
# NOTE: the DTOs live in package com.diskcalc.data (not com.diskcalc.data.dto),
# so the rules below must target the real package or R8 strips the serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class com.diskcalc.data.HistoryEntryDto {
    *** Companion;
}
-keepclassmembers class com.diskcalc.data.HistoryFileDto {
    *** Companion;
}
-keepclasseswithmembers class com.diskcalc.data.HistoryEntryDto {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.diskcalc.data.HistoryFileDto {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.diskcalc.data.HistoryEntryDto$$serializer { *; }
-keep,includedescriptorclasses class com.diskcalc.data.HistoryFileDto$$serializer { *; }
