# MovieTix ProGuard / R8 Rules

# Preserve line numbers and source file names for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# --- Kotlinx Serialization ---
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class **$serializer {
    public static final **$serializer INSTANCE;
}
-keepclasseswithmembers class * {
    public static final **$serializer Companion;
}

# --- Koin Dependency Injection ---
-dontwarn org.koin.**

# --- Ktor Client & OkHttp ---
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# --- Coil 3 Image Loading ---
-dontwarn coil3.**
-keep class coil3.** { *; }

# --- Jetpack DataStore Preferences ---
-dontwarn androidx.datastore.**

# --- Compose Multiplatform ---
-dontwarn androidx.compose.**
