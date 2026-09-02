package com.ravanbarvar.patientmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ravanbarvar.patientmanager.data.local.entity.PatientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {

    @Query("SELECT * FROM patients ORDER BY firstName || ' ' || lastName ASC")
    fun observeAll(): Flow<List<PatientEntity>>

    @Query(
        """
        SELECT * FROM patients
        WHERE firstName LIKE '%' || :query || '%'
           OR lastName LIKE '%' || :query || '%'
           OR phone LIKE '%' || :query || '%'
        ORDER BY firstName || ' ' || lastName ASC
        """
    )
    fun search(query: String): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id")
    fun observeById(id: Long): Flow<PatientEntity?>

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getById(id: Long): PatientEntity?

    @Query("SELECT COUNT(*) FROM patients")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(patient: PatientEntity): Long

    @Update
    suspend fun update(patient: PatientEntity)

    @Delete
    suspend fun delete(patient: PatientEntity)
}
