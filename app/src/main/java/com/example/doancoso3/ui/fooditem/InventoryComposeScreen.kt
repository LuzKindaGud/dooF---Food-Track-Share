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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancoso3.R

// --- Models for UI ---
data class FoodItem(
    val id: String,
    val name: String,
    val details: String,
    val daysLeft: Int,
    val category: String, // Fridge, Pantry, Freezer
    val isSelected: Boolean = false
)

@Composable
fun InventoryComposeScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Fridge", "Pantry", "Freezer", "Expiring Soon")
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Mock Data based on the design
    val items = remember {
        listOf(
            FoodItem("1", "Large Eggs (12pk)", "Added 2 days ago", 4, "Fridge"),
            FoodItem("2", "Greek Yogurt", "Added 1 day ago", 8, "Fridge"),
            FoodItem("3", "Organic Milk", "Added 3 days ago", 2, "Fridge"),
            FoodItem("4", "Sourdough Bread", "Added 1 day ago", 5, "Pantry"),
            FoodItem("5", "Chicken Breast", "Added 5 days ago", 1, "Freezer")
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Organic Blobs (Atmospheric background)
        OrganicBlobs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Persistent Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search 142 items..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Categories Filter (Chips)
            CategoryFilter(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Inventory Sections
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    InventorySection(
                        title = "Fridge",
                        icon = Icons.Default.Kitchen,
                        itemCount = 12,
                        items = items.filter { it.category == "Fridge" }
                    )
                }
                item {
                    InventorySection(
                        title = "Pantry",
                        icon = painterResource(R.drawable.ic_inventory),
                        itemCount = 24,
                        items = items.filter { it.category == "Pantry" },
                        initiallyExpanded = false
                    )
                }
                item {
                    InventorySection(
                        title = "Freezer",
                        icon = Icons.Default.AcUnit,
                        itemCount = 8,
                        items = items.filter { it.category == "Freezer" },
                        initiallyExpanded = false
                    )
                }
            }
        }
    }
}

@Composable
private fun OrganicBlobs() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = 200.dp, y = (-50).dp)
                .size(300.dp)
                .blur(80.dp)
                .background(colorResource(R.color.primary_fixed_dim).copy(alpha = 0.15f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = (-100).dp)
                .size(250.dp)
                .blur(80.dp)
                .background(colorResource(R.color.on_tertiary_container).copy(alpha = 0.15f), CircleShape)
        )
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = colorResource(R.color.on_surface_variant)) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = colorResource(R.color.on_surface_variant)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colorResource(R.color.surface_container),
            unfocusedContainerColor = colorResource(R.color.surface_container),
            focusedBorderColor = colorResource(R.color.lime_primary),
            unfocusedBorderColor = colorResource(R.color.outline_variant),
            cursorColor = colorResource(R.color.lime_primary),
            focusedTextColor = colorResource(R.color.on_surface),
            unfocusedTextColor = colorResource(R.color.on_surface)
        ),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
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
                category == "Expiring Soon" -> colorResource(R.color.error_container)
                else -> colorResource(R.color.lime_primary).copy(alpha = 0.5f)
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) colorResource(R.color.lime_primary)
                        else colorResource(R.color.surface_container_high)
                    )
                    .border(
                        width = if (!isSelected) 3.dp else 0.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = category,
                    color = if (isSelected) Color(0xFF01180A)
                    else if (category == "Expiring Soon") colorResource(R.color.error)
                    else colorResource(R.color.on_surface),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
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
    items: List<FoodItem>,
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
private fun FoodItemRow(item: FoodItem) {
    var checked by remember { mutableStateOf(false) }
    
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
                text = item.details,
                color = colorResource(R.color.on_surface_variant),
                fontSize = 12.sp
            )
        }

        Text(
            text = "${item.daysLeft}D LEFT",
            color = if (item.daysLeft <= 4) colorResource(R.color.error) else colorResource(R.color.primary_fixed_dim),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
