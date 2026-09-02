package com.ravanbarvar.patientmanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ravanbarvar.patientmanager.data.local.dao.AdminDao
import com.ravanbarvar.patientmanager.data.local.dao.AppointmentDao
import com.ravanbarvar.patientmanager.data.local.dao.DocumentDao
import com.ravanbarvar.patientmanager.data.local.dao.PatientDao
import com.ravanbarvar.patientmanager.data.local.entity.AppointmentEntity
import com.ravanbarvar.patientmanager.data.local.entity.DocumentEntity
import com.ravanbarvar.patientmanager.data.local.entity.AdminUserEntity
import com.ravanbarvar.patientmanager.data.local.entity.PatientEntity
import com.ravanbarvar.patientmanager.util.PasswordHasher

const val DEFAULT_ADMIN_USERNAME = "admin"
const val DEFAULT_ADMIN_PASSWORD = "admin123"

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE patients ADD COLUMN photoUri TEXT")
        db.execSQL("ALTER TABLE appointments ADD COLUMN feeAmount INTEGER")
        db.execSQL("ALTER TABLE appointments ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [PatientEntity::class, AppointmentEntity::class, DocumentEntity::class, AdminUserEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun documentDao(): DocumentDao
    abstract fun adminDao(): AdminDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }
        }

        private fun build(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ravanbarvar.db"
            ).addMigrations(MIGRATION_1_2).addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Runs synchronously as part of database creation, before any other
                    // query can reach this database — so the seed is guaranteed to be
                    // there by the time the login screen queries for it. (A previous
                    // version seeded this from a fire-and-forget coroutine, which raced
                    // the very first login attempt on a fresh install.)
                    val salt = PasswordHasher.generateSalt()
                    val hash = PasswordHasher.hash(DEFAULT_ADMIN_PASSWORD, salt)
                    db.execSQL(
                        "INSERT INTO admin_user (username, passwordHash, salt, fullName, createdAt) VALUES (?, ?, ?, ?, ?)",
                        arrayOf(DEFAULT_ADMIN_USERNAME, hash, salt, "مدیر کلینیک", System.currentTimeMillis())
                    )
                }
            }).build()
        }
    }
}
