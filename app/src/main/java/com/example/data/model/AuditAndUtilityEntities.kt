package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audit_log",
    indices = [
        Index("createdAtEpochMs"),
        Index("entityType")
    ]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val entityType: String,
    val entityId: Long,
    val action: String, // CREATED, SETTLED, REVERSED, VOIDED, RESCHEDULED, RESTORED
    val amountPaisa: Long? = null
)

@Entity(
    tableName = "user_reminder",
    indices = [
        Index("dateEpochDay")
    ]
)
data class UserReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val dateEpochDay: Long,
    val done: Boolean = false
)

@Entity(
    tableName = "import_fingerprint",
    indices = [
        Index(value = ["fingerprint"], unique = true)
    ]
)
data class ImportFingerprintEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fingerprint: String,
    val transactionId: Long,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
