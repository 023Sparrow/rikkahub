# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# keep kotlinx serializable classes
-keep @kotlinx.serialization.Serializable class * {*;}

# keep jlatexmath
-keep class org.scilab.forge.jlatexmath.** {*;}

# keep memory enhancement classes (防止Release版本崩溃)
-keep class me.rerere.rikkahub.data.ai.memory.** {*;}
-keep class me.rerere.rikkahub.data.db.entity.MemorySheet {*;}
-keep class me.rerere.rikkahub.data.db.entity.MemoryCell {*;}
-keep class me.rerere.rikkahub.data.db.entity.MemoryColumn {*;}
-keep class me.rerere.rikkahub.data.db.entity.SheetType {*;}
-keep class me.rerere.rikkahub.data.db.dao.MemorySheetDao {*;}
-keep class me.rerere.rikkahub.data.db.dao.MemoryCellDao {*;}
-keep class me.rerere.rikkahub.data.db.dao.MemoryColumnDao {*;}

# keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# keep Koin dependency injection
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# keep Firebase classes
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

-dontobfuscate
