package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CharityCategory {
    ZAKAT,
    SADQAH,
    FITRANA,
    KHAIRAT,
    CUSTOM
}

enum class CharityStatus {
    PLANNED,
    PAID
}

@Entity(
    tableName = "charity_record",
    indices = [
        Index("paidDateEpochDay"),
        Index("zakatCalculationId")
    ]
)
data class CharityRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: CharityCategory,
    val customCategoryName: String? = null,
    val recipientName: String = "",
    val amountPaisa: Long,
    val status: CharityStatus = CharityStatus.PLANNED,
    val plannedDateEpochDay: Long? = null,
    val paidDateEpochDay: Long? = null,
    val accountId: Long? = null,
    val transactionId: Long? = null,
    val zakatCalculationId: Long? = null,
    val note: String = ""
)

@Entity(tableName = "zakat_calculation")
data class ZakatCalculationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val label: String, // e.g. "Ramadan 1448"
    val standard: String, // "SILVER" | "GOLD"
    val weightMgUsed: Long,
    val ratePerGramPaisa: Long,
    val rateEnteredDateEpochDay: Long,
    val rateSource: String = "USER_ENTERED",
    val nisabThresholdPaisa: Long,
    val eligibleWealthPaisa: Long,
    val zakatPayablePaisa: Long,
    val nisabReached: Boolean,
    val hawlConfirmedByUser: Boolean,
    val assumptionsJson: String, // immutable snapshot of included/excluded lines
    val note: String = ""
)
