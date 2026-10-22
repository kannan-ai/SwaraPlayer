# ======================================================================================
# UNIVERSAL WARNING-FREE R8 / PROGUARD RULES
# ======================================================================================

# 1. Suppress unresolved class warnings for optional libraries
-dontwarn androidx.media3.**
-dontwarn com.google.gson.**
-dontwarn com.squareup.moshi.**
-dontwarn kotlinx.serialization.**
-dontwarn org.webrtc.**
-dontwarn coil.**
-dontwarn androidx.room.**
-dontwarn androidx.compose.**

# 2. Targeted Room Persistence Protection (Resolves "Overly Broad" Linter Warnings)
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
    @androidx.room.ProvidedTypeConverter *;
}

# 3. Targeted Serialization & DTO Protection
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.squareup.moshi.Json <fields>;
    @kotlinx.serialization.SerialName <fields>;
}

# Explicitly keep database entities and UI parcelables without broad wildcards
-keep class **.data.local.entity.** { *; }
-keep class **.data.local.relation.** { *; }
-keep class **.data.local.dto.** { *; }
-keep class **.ui.model.** { *; }
-keep class com.example.swaraplayer.data.** { *; }

# 4. Optional Media3 / Audio / Video (Kept only if present)
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.extractor.** { *; }
-keep class androidx.media3.common.** { *; }

# 5. Coil & Coroutines (Kept only if present)
-keep class coil.** { *; }
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions
-keepclassmembers class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepclassmembers class kotlinx.coroutines.CoroutineExceptionHandler { *; }

# 6. Native JNI & WebRTC
-keepclasseswithmembernames class * {
    native <methods>;
}

# 7. Compose Runtime
-keepclassmembers class androidx.compose.animation.core.** { *; }