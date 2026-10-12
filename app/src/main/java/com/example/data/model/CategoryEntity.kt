package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val iconKey: String,
    val colorHex: String,
    val monthlyLimitPaisa: Long? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
