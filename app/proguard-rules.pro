# ============================================================
# Word Search - R8 / ProGuard rules
#
# IMPORTANT: Do NOT add blanket "-keep class ... { *; }" rules
# for whole libraries (Compose, AdMob, etc.). Libraries ship
# their own consumer rules, and blanket keeps disable
# obfuscation, which triggers the Play Console warning:
# "App optimisation is below our threshold - Obfuscation (1%)".
# ============================================================

# --- Attributes needed for reflection, annotations and stack traces ---
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# Keep source file + line numbers so Play Console crash reports
# (retraced with mapping.txt) stay readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Google Mobile Ads (AdMob) ---
# The play-services-ads AAR bundles its own consumer ProGuard rules;
# we only silence warnings here. No -keep needed.
-dontwarn com.google.android.gms.ads.**

# --- WebView JavaScript interfaces (used by AdMob web views) ---
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# --- ViewModels ---
# androidx.lifecycle instantiates ViewModels REFLECTIVELY, so R8 must not
# strip these constructors. Without this rule the release build crashes at
# launch with "Cannot create an instance of class ...GameViewModel".
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# --- Room / WorkManager (pulled in transitively by play-services-ads) ---
# WorkManager creates its Room database REFLECTIVELY via
# Class.forName("androidx.work.impl.WorkDatabase_Impl"). R8 strips the
# generated _Impl class/constructor without these rules, crashing right
# after the splash screen with:
#   "Failed to create an instance of androidx.work.impl.WorkDatabase"
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Database class * { <init>(); }
-dontwarn androidx.room.**
-dontwarn androidx.work.**

# androidx.startup initializers are also discovered/instantiated reflectively.
-keep class * implements androidx.startup.Initializer { <init>(); }
-dontwarn androidx.startup.**

# --- Kotlin Coroutines (official recommended rules) ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# --- Kotlin metadata ---
-dontwarn kotlin.**
-dontwarn kotlin.reflect.**
