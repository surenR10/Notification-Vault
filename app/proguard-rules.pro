# Keep Room entities and DAOs
-keep class com.shs.notificationvault.data.** { *; }

# Keep Kotlin data classes used by Room
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
