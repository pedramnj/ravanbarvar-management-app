package com.ravanbarvar.patientmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ravanbarvar.patientmanager.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

data class AppointmentWithPatientName(
    val id: Long,
    val patientId: Long,
    val dateEpochDay: Long,
    val startMinutes: Int,
    val durationMinutes: Int,
    val notes: String,
    val status: String,
    val createdAt: Long,
    val feeAmount: Long?,
    val isPaid: Boolean,
    val firstName: String,
    val lastName: String
) {
    fun toEntity(): AppointmentEntity = AppointmentEntity(
        id = id,
        patientId = patientId,
        dateEpochDay = dateEpochDay,
        startMinutes = startMinutes,
        durationMinutes = durationMinutes,
        notes = notes,
        status = status,
        createdAt = createdAt,
        feeAmount = feeAmount,
        isPaid = isPaid
    )

    val patientFullName: String get() = "$firstName $lastName"
}

@Dao
interface AppointmentDao {

    @Query(
        """
        SELECT a.*, p.firstName as firstName, p.lastName as lastName
        FROM appointments a
        INNER JOIN patients p ON p.id = a.patientId
        WHERE a.dateEpochDay = :epochDay
        ORDER BY a.startMinutes ASC
        """
    )
    fun observeForDate(epochDay: Long): Flow<List<AppointmentWithPatientName>>

    @Query(
        """
        SELECT a.*, p.firstName as firstName, p.lastName as lastName
        FROM appointments a
        INNER JOIN patients p ON p.id = a.patientId
        WHERE a.dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY a.dateEpochDay ASC, a.startMinutes ASC
        """
    )
    fun observeForRange(startEpochDay: Long, endEpochDay: Long): Flow<List<AppointmentWithPatientName>>

    @Query(
        """
        SELECT a.*, p.firstName as firstName, p.lastName as lastName
        FROM appointments a
        INNER JOIN patients p ON p.id = a.patientId
        WHERE a.patientId = :patientId
        ORDER BY a.dateEpochDay DESC, a.startMinutes DESC
        """
    )
    fun observeForPatient(patientId: Long): Flow<List<AppointmentWithPatientName>>

    @Query(
        """
        SELECT a.*, p.firstName as firstName, p.lastName as lastName
        FROM appointments a
        INNER JOIN patients p ON p.id = a.patientId
        WHERE a.patientId = :patientId AND a.status = 'SCHEDULED' AND
              (a.dateEpochDay > :todayEpochDay OR (a.dateEpochDay = :todayEpochDay AND a.startMinutes >= :nowMinutes))
        ORDER BY a.dateEpochDay ASC, a.startMinutes ASC
        LIMIT 1
        """
    )
    fun observeNextUpcomingForPatient(patientId: Long, todayEpochDay: Long, nowMinutes: Int): Flow<AppointmentWithPatientName?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(appointment: AppointmentEntity): Long

    @Update
    suspend fun update(appointment: AppointmentEntity)

    @Delete
    suspend fun delete(appointment: AppointmentEntity)

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getById(id: Long): AppointmentEntity?

    @Query(
        """
        SELECT a.*, p.firstName as firstName, p.lastName as lastName
        FROM appointments a
        INNER JOIN patients p ON p.id = a.patientId
        WHERE a.status = 'SCHEDULED' AND a.dateEpochDay >= :fromEpochDay
        """
    )
    suspend fun getScheduledFrom(fromEpochDay: Long): List<AppointmentWithPatientName>
}
