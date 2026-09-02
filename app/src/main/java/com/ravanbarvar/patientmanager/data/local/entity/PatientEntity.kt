package com.ravanbarvar.patientmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val nationalId: String,
    val birthDateEpochDay: Long?,
    val gender: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
)

object Gender {
    const val MALE = "MALE"
    const val FEMALE = "FEMALE"
    const val OTHER = "OTHER"
}
