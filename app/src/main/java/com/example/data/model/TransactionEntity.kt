package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index("fromAccountId"),
        Index("toAccountId"),
        Index("categoryId"),
        Index("dateEpochMs"),
        Index("recurringOccurrenceId"),
        Index("debtId"),
        Index("transferId"),
        Index("sourceType"),
        Index("voidedAtEpochMs")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amountPaisa: Long,
    val fromAccountId: Long? = null,
    val toAccountId: Long? = null,
    val categoryId: Long? = null,
    val note: String = "",
    val receiptPath: String? = null,
    val recurringOccurrenceId: Long? = null,
    val debtId: Long? = null,
    val sourceType: String? = null,
    val sourceId: Long? = null,
    val transferId: String? = null,
    val reversalOfId: Long? = null,
    val voidedAtEpochMs: Long? = null,
    val dateEpochMs: Long = System.currentTimeMillis(),
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)
