package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipt_attachments",
    indices = [Index("transactionId")]
)
data class ReceiptAttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val localRelativePath: String,
    val mimeType: String = "image/jpeg",
    val fileSizeBytes: Long = 0L,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
