package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class InstallmentStatus {
    SCHEDULED,
    PAID,
    PARTIAL,
    MISSED,
    RESCHEDULED,
    CANCELLED
}

@Entity(
    tableName = "karz_installment",
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("debtId"),
        Index(value = ["debtId", "number"], unique = true),
        Index("dueDateEpochDay")
    ]
)
data class KarzInstallmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val debtId: Long,
    val number: Int,
    val totalCount: Int,
    val dueDateEpochDay: Long,
    val scheduledPaisa: Long,
    val paidPaisa: Long = 0,
    val status: InstallmentStatus = InstallmentStatus.SCHEDULED,
    val note: String = ""
)
