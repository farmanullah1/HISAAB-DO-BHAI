package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "needs")
data class NeedEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val expectedPricePaisa: Long? = null,
    val whereToGet: String = "",
    val note: String = "",
    val isDone: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val doneAtEpochMs: Long? = null
)
