# Musify ProGuard & R8 Optimization and Hardening Rules

# 1. GeckoView Engine Native Bridges, JNI & Reflection
-keep class org.mozilla.geckoview.** { *; }
-dontwarn org.mozilla.geckoview.**

# 2. Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# 3. Jetpack Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }

# 4. Strip verbose and debug logging calls in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# 5. Prevent leaking internal class/method structures and sensitive stack traces
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
