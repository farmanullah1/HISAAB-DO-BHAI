package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DebtEventType {
    ORIGINAL_ADVANCE,    // Initial creation
    ADDITIONAL_ADVANCE,  // Added more to loan/borrowing
    REPAYMENT,           // Partial or full payment
    ADJUSTMENT,          // Correction/adjustment
    WRITE_OFF            // Forgiven or unrecoverable
}

@Entity(
    tableName = "debt_events",
    indices = [
        Index("debtId"),
        Index("dateEpochMs"),
        Index("transactionId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DebtEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val debtId: Long,
    val type: DebtEventType,
    val amountPaisa: Long,
    val dateEpochMs: Long = System.currentTimeMillis(),
    val note: String = "",
    val accountId: Long? = null,
    val transactionId: Long? = null,
    val receiptPath: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
