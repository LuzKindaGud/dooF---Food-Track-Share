package com.example.doancoso3.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doancoso3.R

@Composable
fun HomeComposeScreen(viewModel: HomeViewModel = hiltViewModel()) {
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
        InventoryStatusCard(totalItems, expiringCount)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                label = "WASTE SAVED",
                value = wasteSaved
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                label = "SAVINGS",
                value = savings
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Family Activity", R.drawable.ic_family_group)
        Spacer(modifier = Modifier.height(12.dp))
        ActivityCard(activities)

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Recent Inventory", null, "SEE ALL")
        Spacer(modifier = Modifier.height(12.dp))
        if (recentItems.isNotEmpty()) {
            RecentItemsCard(recentItems)
            Spacer(modifier = Modifier.height(20.dp))
        }
        MealPrepCard(mealPrepCount)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InventoryStatusCard(totalItems: Int, expiringCount: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container_high).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "INVENTORY STATUS",
                color = colorResource(R.color.on_surface_variant).copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$totalItems Items",
                color = colorResource(R.color.on_surface),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$expiringCount Expiring in 48h",
                color = colorResource(R.color.on_surface_variant).copy(alpha = 0.9f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SummaryCard(modifier: Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container_high).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                color = colorResource(R.color.lime_primary),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = colorResource(R.color.on_surface),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
private fun SectionHeader(title: String, iconRes: Int?, actionText: String? = null) {
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
                fontSize = 12.sp
            )
        } else if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = title,
                tint = colorResource(R.color.on_surface_variant)
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
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container_high).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            displayActivities.take(3).forEachIndexed { index, item ->
                Row {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(width = 4.dp, height = 40.dp)
                            .background(
                                color = if (index == 0) {
                                    colorResource(R.color.lime_primary)
                                } else {
                                    colorResource(R.color.outline_variant)
                                },
                                shape = RoundedCornerShape(999.dp)
                            )
                    )
                    Column {
                        Text(
                            text = item.title,
                            color = colorResource(R.color.on_surface),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = item.subtitle,
                            color = colorResource(R.color.on_surface_variant),
                            fontSize = 12.sp
                        )
                    }
                }
                if (index < displayActivities.take(3).lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = colorResource(R.color.outline_variant)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentItemsCard(items: List<RecentInventoryItem>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container_high).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            items.take(3).forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = when (item.status) {
                                    ExpiryDisplayStatus.EXPIRED -> colorResource(R.color.error)
                                    ExpiryDisplayStatus.WARNING -> colorResource(R.color.lime_primary)
                                    ExpiryDisplayStatus.GOOD -> colorResource(R.color.lime_primary)
                                },
                                shape = RoundedCornerShape(999.dp)
                            )
                    )
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(
                            text = item.name,
                            color = colorResource(R.color.on_surface),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.expiryText,
                            color = colorResource(R.color.on_surface_variant),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = item.quantity,
                        color = if (item.status == ExpiryDisplayStatus.EXPIRED) {
                            colorResource(R.color.error)
                        } else {
                            colorResource(R.color.lime_primary)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                if (index < items.take(3).lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = colorResource(R.color.outline_variant)
                    )
                }
            }
        }
    }
}

@Composable
private fun MealPrepCard(mealPrepCount: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container_high).copy(alpha = 0.6f)),
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
                    color = colorResource(R.color.on_surface),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You have $mealPrepCount ingredients expiring soon that work great in a stir-fry.",
                    color = colorResource(R.color.on_surface_variant),
                    fontSize = 13.sp
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_salad_bowl),
                contentDescription = "Meal prep",
                tint = colorResource(R.color.lime_primary),
                modifier = Modifier.size(72.dp)
            )
        }
    }
}
