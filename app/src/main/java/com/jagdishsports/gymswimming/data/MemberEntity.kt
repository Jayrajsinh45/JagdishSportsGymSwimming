package com.jagdishsports.gymswimming.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "members",
    indices = [
        Index(value = ["category"]),
        Index(value = ["endDateEpochDay"])
    ]
)
data class MemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phoneNumber: String,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long,
    val feesPaid: Long,
    val category: String,
    val photoPath: String? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
