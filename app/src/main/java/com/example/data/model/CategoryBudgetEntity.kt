package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category_budget",
    indices = [
        Index(value = ["categoryId", "monthKey"], unique = true)
    ]
)
data class CategoryBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val monthKey: String, // "2026-10", or "*" for the default every month
    val limitPaisa: Long
)
