package com.example.doancoso3.ui.fooditem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps a food item name to a clean Material vector icon. Shared across Home, Inventory
 * and the Add/Edit sheet so food icons stay consistent and crisp everywhere.
 */
fun foodIconFor(name: String): ImageVector {
    val n = name.lowercase()
    return when {
        n.contains("egg") -> Icons.Filled.Egg
        n.contains("milk") || n.contains("yogurt") || n.contains("dairy") ||
            n.contains("juice") || n.contains("drink") || n.contains("water") -> Icons.Filled.LocalDrink
        n.contains("fish") || n.contains("seafood") || n.contains("salmon") ||
            n.contains("shrimp") || n.contains("sushi") -> Icons.Filled.SetMeal
        n.contains("meat") || n.contains("chicken") || n.contains("beef") ||
            n.contains("pork") || n.contains("ham") || n.contains("sausage") -> Icons.Filled.LunchDining
        n.contains("veg") || n.contains("salad") || n.contains("fruit") ||
            n.contains("leaf") || n.contains("green") -> Icons.Filled.Eco
        n.contains("rice") || n.contains("kimbab") || n.contains("kimbap") ||
            n.contains("noodle") || n.contains("ramen") || n.contains("pho") -> Icons.Filled.RiceBowl
        n.contains("bread") || n.contains("cake") || n.contains("bakery") ||
            n.contains("pastry") || n.contains("bun") -> Icons.Filled.BakeryDining
        n.contains("pizza") -> Icons.Filled.LocalPizza
        n.contains("ice") || n.contains("cream") -> Icons.Filled.Icecream
        else -> Icons.Filled.Restaurant
    }
}
