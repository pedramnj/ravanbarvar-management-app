package com.ravanbarvar.patientmanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId"), Index("dateEpochDay")]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val dateEpochDay: Long,
    val startMinutes: Int,
    val durationMinutes: Int,
    val notes: String,
    val status: String,
    val createdAt: Long,
    val feeAmount: Long? = null,
    val isPaid: Boolean = false
)

object AppointmentStatus {
    const val SCHEDULED = "SCHEDULED"
    const val DONE = "DONE"
    const val CANCELED = "CANCELED"
}
