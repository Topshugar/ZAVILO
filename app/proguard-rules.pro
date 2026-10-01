# Keep AndroidX Security and other libraries intact
-keep class androidx.security.crypto.** { *; }
-keep class com.google.android.material.** { *; }

# Preserve important Android framework/service classes when minifying
-keep class com.zavilo.app.** { *; }

# Keep app entry points intact
-keep class com.zavilo.app.MainActivity { *; }
-keep class com.zavilo.app.ZaviloNotificationService { *; }
