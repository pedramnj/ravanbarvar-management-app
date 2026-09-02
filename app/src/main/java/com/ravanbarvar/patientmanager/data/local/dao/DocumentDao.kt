package com.ravanbarvar.patientmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ravanbarvar.patientmanager.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun observeForPatient(patientId: Long): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(document: DocumentEntity): Long

    @Delete
    suspend fun delete(document: DocumentEntity)
}
