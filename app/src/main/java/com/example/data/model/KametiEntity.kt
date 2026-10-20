package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class KametiCadence {
    WEEKLY,
    MONTHLY
}

enum class KametiStatus {
    ACTIVE,
    PAUSED,
    COMPLETED
}

enum class ContributionStatus {
    PENDING,
    PAID,
    PARTIAL,
    MISSED,
    SKIPPED
}

enum class PayoutStatus {
    EXPECTED,
    RECEIVED,
    NOT_RECEIVED
}

@Entity(
    tableName = "kameti",
    indices = [
        Index("status"),
        Index("archived")
    ]
)
data class KametiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val memberCount: Int,
    val contributionPaisa: Long,
    val cadence: KametiCadence,
    val startDateEpochDay: Long,
    val totalInstallments: Int,
    val myPayoutInstallment: Int, // 1..totalInstallments
    val expectedPayoutPaisa: Long? = null,
    val status: KametiStatus = KametiStatus.ACTIVE,
    val note: String = "",
    val archived: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "kameti_contribution",
    foreignKeys = [
        ForeignKey(
            entity = KametiEntity::class,
            parentColumns = ["id"],
            childColumns = ["kametiId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("kametiId"),
        Index(value = ["kametiId", "installmentIndex"], unique = true),
        Index("dueDateEpochDay")
    ]
)
data class KametiContributionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kametiId: Long,
    val installmentIndex: Int,
    val dueDateEpochDay: Long,
    val expectedPaisa: Long,
    val paidPaisa: Long = 0,
    val status: ContributionStatus = ContributionStatus.PENDING,
    val lastPaidDateEpochDay: Long? = null,
    val note: String = ""
)

@Entity(
    tableName = "kameti_payout",
    foreignKeys = [
        ForeignKey(
            entity = KametiEntity::class,
            parentColumns = ["id"],
            childColumns = ["kametiId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("kametiId", unique = true)
    ]
)
data class KametiPayoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kametiId: Long,
    val expectedDateEpochDay: Long,
    val expectedPaisa: Long?,
    val receivedPaisa: Long? = null,
    val receivedDateEpochDay: Long? = null,
    val status: PayoutStatus = PayoutStatus.EXPECTED,
    val transactionId: Long? = null
)
