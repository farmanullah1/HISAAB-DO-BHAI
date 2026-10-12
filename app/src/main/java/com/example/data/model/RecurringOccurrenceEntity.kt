package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_occurrences",
    indices = [
        Index("ruleId"),
        Index("status"),
        Index("occurrenceDateEpochMs"),
        Index("transactionId")
    ]
)
data class RecurringOccurrenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleId: Long,
    val occurrenceDateEpochMs: Long,
    val status: OccurrenceStatus = OccurrenceStatus.PENDING,
    val transactionId: Long? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
