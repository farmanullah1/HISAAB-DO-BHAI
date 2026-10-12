package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {

    fun getIcon(iconKey: String?): ImageVector {
        return when (iconKey?.lowercase()) {
            "restaurant", "food" -> Icons.Default.Restaurant
            "shopping_cart", "grocery", "groceries" -> Icons.Default.ShoppingCart
            "directions_car", "transport", "fuel" -> Icons.Default.DirectionsCar
            "receipt_long", "bills", "utilities" -> Icons.Default.ReceiptLong
            "home", "rent", "housing" -> Icons.Default.Home
            "medical_services", "health" -> Icons.Default.MedicalServices
            "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
            "school", "education" -> Icons.Default.School
            "payments", "salary" -> Icons.Default.Payments
            "laptop", "freelance" -> Icons.Default.Laptop
            "redeem", "gift" -> Icons.Default.CardGiftcard
            "replay", "refund" -> Icons.Default.Replay
            "bank" -> Icons.Default.AccountBalance
            "wallet" -> Icons.Default.Wallet
            else -> Icons.Default.Category
        }
    }
}
