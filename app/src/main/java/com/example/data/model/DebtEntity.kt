package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DebtDirection {
    I_GAVE,  // Money I lent to someone (they owe me) -> Receivable
    I_TOOK   // Money I borrowed from someone (I owe them) -> Payable
}

enum class DebtStatus {
    ACTIVE,
    PARTIALLY_PAID,
    SETTLED,
    WRITTEN_OFF
}

enum class DebtKind {
    MONEY,
    ITEM_SERVICE
}

@Entity(
    tableName = "debts",
    indices = [
        Index("direction"),
        Index("status"),
        Index("kind"),
        Index("dateEpochMs"),
        Index("dueDateEpochMs")
    ]
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val direction: DebtDirection,
    val originalPrincipalPaisa: Long,
    val kind: DebtKind = DebtKind.MONEY,
    val itemDescription: String = "",
    val itemQuantity: Double? = null,
    val itemUnit: String = "",
    val estimatedValuePaisa: Long? = null,
    val dateEpochMs: Long = System.currentTimeMillis(),
    val dueDateEpochMs: Long? = null,
    val description: String = "",
    val phoneNumber: String = "",
    val notes: String = "",
    val status: DebtStatus = DebtStatus.ACTIVE,
    val linkedAccountId: Long? = null,
    val isInterestEnabled: Boolean = false,
    val interestRatePercent: Double = 0.0,
    val settledAtEpochMs: Long? = null,
    val writtenOffReason: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)
