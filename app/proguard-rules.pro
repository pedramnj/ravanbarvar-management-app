# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Keep entity/DAO model classes used via reflection by Room
-keep class com.ravanbarvar.patientmanager.data.local.entity.** { *; }
