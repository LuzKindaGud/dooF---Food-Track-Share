package com.example.doancoso3.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doancoso3.R

// Pale ivory surface used by the high-contrast hero / accent cards.
private val Ivory = Color(0xFFFDFCF0)

@Composable
fun HomeComposeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddItemClick: () -> Unit = {},
    onSeeAllClick: () -> Unit = {}
) {
    val totalItems by viewModel.totalItems.observeAsState(0)
    val expiringCount by viewModel.expiringCount.observeAsState(0)
    val wasteSaved by viewModel.wasteSaved.observeAsState("0kg")
    val savings by viewModel.savings.observeAsState("$0.00")
    val activities by viewModel.familyActivities.observeAsState(emptyList())
    val recentItems by viewModel.recentItems.observeAsState(emptyList())
    val mealPrepCount by viewModel.mealPrepCount.observeAsState(0)

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Hero bento
        InventoryStatusCard(totalItems, expiringCount, onAddItemClick)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(Modifier.weight(1f), "WASTE SAVED", wasteSaved)
            SummaryCard(Modifier.weight(1f), "SAVINGS", savings)
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Family Activity", iconRes = R.drawable.ic_family_group)
        Spacer(modifier = Modifier.height(12.dp))
        ActivityCard(activities)

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Recent Inventory", actionText = "SEE ALL", onActionClick = onSeeAllClick)
        Spacer(modifier = Modifier.height(12.dp))
        if (recentItems.isNotEmpty()) {
            RecentItemsList(recentItems)
            Spacer(modifier = Modifier.height(20.dp))
        }
        MealPrepCard(mealPrepCount, onAddItemClick)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InventoryStatusCard(totalItems: Int, expiringCount: Int, onAddItemClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Ivory),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "INVENTORY STATUS",
                color = colorResource(R.color.on_tertiary_fixed_variant),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$totalItems Items",
                        color = colorResource(R.color.on_tertiary_fixed),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$expiringCount Expiring in 48h",
                        color = colorResource(R.color.on_tertiary_fixed_variant),
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colorResource(R.color.lime_primary))
                        .clickable { onAddItemClick() }
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "ADD ITEM",
                        color = colorResource(R.color.on_primary_fixed),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(modifier: Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container).copy(alpha = 0.7f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, colorResource(R.color.outline_variant).copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                color = colorResource(R.color.primary_fixed),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = colorResource(R.color.on_surface),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    iconRes: Int? = null,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = colorResource(R.color.on_surface),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold
        )
        if (actionText != null) {
            Text(
                text = actionText,
                color = colorResource(R.color.lime_primary),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onActionClick() }
            )
        } else if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = title,
                tint = colorResource(R.color.lime_primary)
            )
        }
    }
}

@Composable
private fun ActivityCard(activities: List<FamilyActivityItem>) {
    val displayActivities = if (activities.isNotEmpty()) activities else listOf(
        FamilyActivityItem("No recent activity", "Start adding items to see activity here", true)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Ivory),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val shown = displayActivities.take(3)
            shown.forEachIndexed { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(end = 14.dp)
                            .size(width = 5.dp, height = 40.dp)
                            .clip(CircleShape)
                            .background(
                                when (index) {
                                    0 -> colorResource(R.color.lime_primary)
                                    1 -> colorResource(R.color.on_tertiary_container)
                                    else -> colorResource(R.color.error)
                                }
                            )
                    )
                    Column {
                        Text(
                            text = item.title,
                            color = colorResource(R.color.on_tertiary_fixed),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = item.subtitle,
                            color = colorResource(R.color.on_tertiary_fixed_variant),
                            fontSize = 12.sp
                        )
                    }
                }
                if (index < shown.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = colorResource(R.color.on_tertiary_fixed_variant).copy(alpha = 0.12f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentItemsList(items: List<RecentInventoryItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.take(4).forEach { item ->
            RecentItemRow(item)
        }
    }
}

@Composable
private fun RecentItemRow(item: RecentInventoryItem) {
    val accent = when (item.status) {
        ExpiryDisplayStatus.EXPIRED -> colorResource(R.color.error)
        else -> colorResource(R.color.lime_primary)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container_low).copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon tile
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorResource(R.color.surface_container_highest)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconForItem(item.name)),
                    contentDescription = null,
                    tint = colorResource(R.color.lime_primary),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        color = colorResource(R.color.on_surface),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (item.status == ExpiryDisplayStatus.EXPIRED) "EXPIRED" else item.quantity,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (item.status == ExpiryDisplayStatus.EXPIRED) "Action Required" else item.expiryText,
                        color = if (item.status == ExpiryDisplayStatus.EXPIRED) colorResource(R.color.error)
                        else colorResource(R.color.on_surface_variant),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    StatusDots(item.status)
                }
            }
        }
    }
}

@Composable
private fun StatusDots(status: ExpiryDisplayStatus) {
    val lime = colorResource(R.color.lime_primary)
    val outline = colorResource(R.color.outline_variant)
    val error = colorResource(R.color.error)
    val dots = when (status) {
        ExpiryDisplayStatus.GOOD -> listOf(lime, lime)
        ExpiryDisplayStatus.WARNING -> listOf(lime, outline)
        ExpiryDisplayStatus.EXPIRED -> listOf(error)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dots.forEach { c ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(c)
            )
        }
    }
}

@Composable
private fun MealPrepCard(mealPrepCount: Int, onViewRecipe: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Ivory),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ready to meal prep?",
                    color = colorResource(R.color.on_tertiary_fixed),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You have $mealPrepCount ingredients expiring soon that work great in a stir-fry.",
                    color = colorResource(R.color.on_tertiary_fixed_variant),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(2.dp, colorResource(R.color.on_tertiary_fixed), CircleShape)
                        .clickable { onViewRecipe() }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "VIEW RECIPE",
                        color = colorResource(R.color.on_tertiary_fixed),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            Icon(
                painter = painterResource(R.drawable.ic_salad_bowl),
                contentDescription = "Meal prep",
                tint = colorResource(R.color.on_tertiary_fixed),
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

/** Picks a food icon based on the item name keywords. */
private fun iconForItem(name: String): Int {
    val n = name.lowercase()
    return when {
        n.contains("egg") -> R.drawable.ic_egg
        n.contains("milk") || n.contains("yogurt") || n.contains("dairy") -> R.drawable.ic_milk_bottle
        n.contains("meat") || n.contains("chicken") || n.contains("beef") || n.contains("pork") || n.contains("fish") -> R.drawable.ic_meat
        n.contains("salad") || n.contains("veg") || n.contains("fruit") || n.contains("leaf") -> R.drawable.ic_salad_bowl
        else -> R.drawable.ic_food_item
    }
}
