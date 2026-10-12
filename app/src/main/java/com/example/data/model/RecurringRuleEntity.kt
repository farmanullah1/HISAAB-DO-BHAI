package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_rules",
    indices = [Index("accountId"), Index("categoryId")]
)
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: TransactionType,
    val amountPaisa: Long,
    val accountId: Long,
    val categoryId: Long,
    val frequency: RecurringFrequency,
    val startDateEpochMs: Long,
    val nextDueEpochMs: Long,
    val isActive: Boolean = true,
    val note: String = "",
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
