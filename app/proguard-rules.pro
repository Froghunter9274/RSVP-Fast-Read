# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>(...);
}

# PDFBox-Android
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
-dontwarn org.apache.pdfbox.rendering.**
-dontwarn org.bouncycastle.**
-dontwarn org.apache.harmony.**

# Jsoup
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# DataStore / Coroutines
-dontwarn kotlinx.coroutines.**

# AndroidX
-dontwarn androidx.paging.PagingSource
