package com.example.doancoso3.ui.fooditem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancoso3.R
import com.example.doancoso3.data.model.FoodItemEntity
import com.example.doancoso3.data.model.StorageLocation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Helper Extensions ---
private fun FoodItemEntity.getDetails(): String {
    val addedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(createdAt))
    return "Qty: $quantity | Added: $addedDate"
}

private fun FoodItemEntity.getDaysLeft(): Int {
    val diff = expiryDate - System.currentTimeMillis()
    return (diff / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(0)
}

@Composable
fun InventoryComposeScreen(
    viewModel: FoodItemViewModel,
    onAddItemClick: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val groupedItems by viewModel.groupedItems.collectAsState()

    val locations = listOf("All") + StorageLocation.entries.map { it.name.lowercase().replaceFirstChar { it.uppercase() } }
    val selectedCategoryName = selectedLocation?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "All"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Persistent Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                placeholder = "Search items..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Categories Filter (Chips)
            CategoryFilter(
                categories = locations,
                selectedCategory = selectedCategoryName,
                onCategorySelected = { name ->
                    val location = if (name == "All") null else StorageLocation.valueOf(name.uppercase())
                    viewModel.updateSelectedLocation(location)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Inventory Sections
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StorageLocation.entries.forEach { location ->
                    val items = groupedItems[location] ?: emptyList()
                    if (items.isNotEmpty() || (selectedLocation == location)) {
                        item {
                            val icon = when (location) {
                                StorageLocation.FRIDGE -> Icons.Default.Kitchen
                                StorageLocation.FREEZER -> Icons.Default.AcUnit
                                StorageLocation.PANTRY -> painterResource(R.drawable.ic_inventory)
                            }
                            InventorySection(
                                title = location.name.lowercase().replaceFirstChar { it.uppercase() },
                                icon = icon,
                                itemCount = items.size,
                                items = items,
                                initiallyExpanded = selectedLocation == location || items.isNotEmpty()
                            )
                        }
                    }
                }
            }
        }

        // Add Item FAB
        FloatingActionButton(
            onClick = onAddItemClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = colorResource(R.color.lime_primary),
            contentColor = Color(0xFF01180A),
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Item")
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = colorResource(R.color.on_surface_variant).copy(alpha = 0.6f)) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = colorResource(R.color.on_surface_variant).copy(alpha = 0.7f)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colorResource(R.color.surface_container).copy(alpha = 0.5f),
            unfocusedContainerColor = colorResource(R.color.surface_container).copy(alpha = 0.3f),
            focusedBorderColor = colorResource(R.color.lime_primary).copy(alpha = 0.5f),
            unfocusedBorderColor = colorResource(R.color.outline_variant).copy(alpha = 0.2f),
            cursorColor = colorResource(R.color.lime_primary),
            focusedTextColor = colorResource(R.color.on_surface),
            unfocusedTextColor = colorResource(R.color.on_surface)
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
private fun CategoryFilter(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            val borderColor = when {
                isSelected -> Color.Transparent
                category == "Expiring Soon" -> colorResource(R.color.error).copy(alpha = 0.5f)
                else -> colorResource(R.color.outline_variant).copy(alpha = 0.3f)
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) colorResource(R.color.lime_primary)
                        else colorResource(R.color.surface_container_high).copy(alpha = 0.3f)
                    )
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    color = if (isSelected) Color(0xFF01180A)
                    else if (category == "Expiring Soon") colorResource(R.color.error)
                    else colorResource(R.color.on_surface).copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}


@Composable
private fun InventorySection(
    title: String,
    icon: Any,
    itemCount: Int,
    items: List<FoodItemEntity>,
    initiallyExpanded: Boolean = true
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorResource(R.color.outline_variant).copy(alpha = 0.3f))
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.surface_container_high).copy(alpha = 0.4f))
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (icon) {
                    is androidx.compose.ui.graphics.painter.Painter -> {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = colorResource(R.color.primary_fixed),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    is androidx.compose.ui.graphics.vector.ImageVector -> {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colorResource(R.color.primary_fixed),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(R.color.primary_fixed),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$itemCount ITEMS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.on_surface_variant),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colorResource(R.color.on_surface_variant)
                )
            }

            // Items List
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    items.forEachIndexed { index, item ->
                        FoodItemRow(item)
                        if (index < items.size - 1) {
                            HorizontalDivider(color = colorResource(R.color.outline_variant).copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodItemRow(item: FoodItemEntity) {
    var checked by remember { mutableStateOf(false) }
    val daysLeft = item.getDaysLeft()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Open edit modal */ }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom Checkbox for Selection
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, colorResource(R.color.outline_variant), RoundedCornerShape(4.dp))
                .clickable { checked = !checked }
                .background(if (checked) colorResource(R.color.primary_fixed_dim) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF01180A),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = colorResource(R.color.primary),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Text(
                text = item.getDetails(),
                color = colorResource(R.color.on_surface_variant),
                fontSize = 12.sp
            )
        }

        Text(
            text = "${daysLeft}D LEFT",
            color = if (daysLeft <= 4) colorResource(R.color.error) else colorResource(R.color.primary_fixed_dim),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
