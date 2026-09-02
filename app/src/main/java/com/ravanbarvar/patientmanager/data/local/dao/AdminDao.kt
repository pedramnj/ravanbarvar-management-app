package com.ravanbarvar.patientmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ravanbarvar.patientmanager.data.local.entity.AdminUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminDao {

    @Query("SELECT * FROM admin_user WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): AdminUserEntity?

    @Query("SELECT * FROM admin_user WHERE id = :id")
    fun observeById(id: Long): Flow<AdminUserEntity?>

    @Query("SELECT * FROM admin_user WHERE id = :id")
    suspend fun getById(id: Long): AdminUserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(admin: AdminUserEntity): Long

    @Update
    suspend fun update(admin: AdminUserEntity)

    @Query("SELECT COUNT(*) FROM admin_user")
    suspend fun count(): Int
}
