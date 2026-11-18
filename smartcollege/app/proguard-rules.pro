# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in getDefaultProguardFile(...)

# Keep data models used in serialization/reflection
-keep class com.vvit.smartcollege.data.model.** { *; }

# Compose rules
-keep class androidx.compose.** { *; }

# Strip verbose and debug logging in release builds to prevent Logcat leakage
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
