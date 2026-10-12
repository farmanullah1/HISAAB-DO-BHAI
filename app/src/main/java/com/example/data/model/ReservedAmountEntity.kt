package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reserved_amounts")
data class ReservedAmountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amountPaisa: Long,
    val targetDateEpochMs: Long? = null,
    val note: String = "",
    val isActive: Boolean = true,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
