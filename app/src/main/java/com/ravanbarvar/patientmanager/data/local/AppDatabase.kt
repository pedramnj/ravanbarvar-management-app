package com.ravanbarvar.patientmanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ravanbarvar.patientmanager.data.local.dao.AdminDao
import com.ravanbarvar.patientmanager.data.local.dao.AppointmentDao
import com.ravanbarvar.patientmanager.data.local.dao.DocumentDao
import com.ravanbarvar.patientmanager.data.local.dao.PatientDao
import com.ravanbarvar.patientmanager.data.local.entity.AdminUserEntity
import com.ravanbarvar.patientmanager.data.local.entity.AppointmentEntity
import com.ravanbarvar.patientmanager.data.local.entity.DocumentEntity
import com.ravanbarvar.patientmanager.data.local.entity.PatientEntity
import com.ravanbarvar.patientmanager.util.PasswordHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val DEFAULT_ADMIN_USERNAME = "admin"
const val DEFAULT_ADMIN_PASSWORD = "admin123"

@Database(
    entities = [PatientEntity::class, AppointmentEntity::class, DocumentEntity::class, AdminUserEntity::class],
    version = 1,
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
            val seedScope = CoroutineScope(Dispatchers.IO)
            lateinit var database: AppDatabase
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ravanbarvar.db"
            ).addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seedScope.launch {
                        val dao = database.adminDao()
                        if (dao.count() == 0) {
                            val salt = PasswordHasher.generateSalt()
                            val hash = PasswordHasher.hash(DEFAULT_ADMIN_PASSWORD, salt)
                            dao.insert(
                                AdminUserEntity(
                                    username = DEFAULT_ADMIN_USERNAME,
                                    passwordHash = hash,
                                    salt = salt,
                                    fullName = "مدیر کلینیک",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }).build()
            return database
        }
    }
}
