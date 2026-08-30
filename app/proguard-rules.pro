# Preserve Line Numbers for Stacktraces and Crashlytics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Google Play Games Services v2
-keep class com.google.android.gms.games.** { *; }
-dontwarn com.google.android.gms.games.**

# Google Play Billing v7
-keep class com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.api.**

# App Data Models, Entities & Enums
-keep class com.example.data.model.** { *; }
-keep class com.example.data.local.** { *; }

# Kotlin Coroutines & Flow
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
