package com.example.doancoso3.ui.main

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doancoso3.R
import com.example.doancoso3.data.model.FoodItemEntity
import com.example.doancoso3.ui.components.FreshVitalityBackground
import com.example.doancoso3.ui.fooditem.AddEditFoodItemDialog
import com.example.doancoso3.ui.fooditem.FoodItemViewModel
import com.example.doancoso3.ui.fooditem.InventoryComposeScreen
import com.example.doancoso3.ui.fooditem.InventoryUiEvent
import com.example.doancoso3.ui.home.HomeComposeScreen
import com.example.doancoso3.ui.profile.ProfileComposeScreen

private enum class MainTab(
    val title: String,
    @DrawableRes val selectedIconRes: Int,
    @DrawableRes val unselectedIconRes: Int
) {
    Home("Home", R.drawable.ic_home_filled, R.drawable.ic_home_filled),
    Inventory("Inventory", R.drawable.ic_inventory, R.drawable.ic_inventory),
    Profile("Profile", R.drawable.ic_profile, R.drawable.ic_profile)
}

@Composable
fun MainComposeScreen(
    inventoryViewModel: FoodItemViewModel = hiltViewModel()
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<FoodItemEntity?>(null) }

    // Centralized one-shot event handling (MVVM): the Add/Edit sheet is hosted here so the
    // "+" FAB works the same on every tab. Close the sheet on save/delete and show a Toast.
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        inventoryViewModel.events.collect { event ->
            when (event) {
                is InventoryUiEvent.ItemSaved -> {
                    showAddSheet = false
                    editingItem = null
                    Toast.makeText(context, "Đã lưu vào kho hàng", Toast.LENGTH_SHORT).show()
                }
                is InventoryUiEvent.ItemDeleted -> {
                    showAddSheet = false
                    editingItem = null
                    Toast.makeText(context, "Đã xóa khỏi kho hàng", Toast.LENGTH_SHORT).show()
                }
                is InventoryUiEvent.Message -> {
                    Toast.makeText(context, event.text, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    FreshVitalityBackground {
        if (isEditingProfile) {
            com.example.doancoso3.ui.profile.EditProfileComposeScreen(
                onBack = { isEditingProfile = false }
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = { MainTopBar() },
                bottomBar = {
                    MainBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                },
                floatingActionButton = {
                    if (selectedTab != MainTab.Inventory) {
                        FloatingActionButton(
                            onClick = {
                                editingItem = null
                                showAddSheet = true
                            },
                            containerColor = colorResource(R.color.lime_primary),
                            contentColor = colorResource(R.color.dark_forest),
                            shape = CircleShape
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_plus),
                                contentDescription = "Add item"
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            fadeIn(tween(180)) togetherWith fadeOut(tween(180))
                        },
                        label = "MainContentFade"
                    ) { targetTab ->
                        when (targetTab) {
                            MainTab.Home -> HomeComposeScreen(
                                onAddItemClick = {
                                    editingItem = null
                                    showAddSheet = true
                                },
                                onSeeAllClick = { selectedTab = MainTab.Inventory }
                            )
                            MainTab.Inventory -> InventoryComposeScreen(
                                viewModel = inventoryViewModel,
                                onAddItemClick = {
                                    editingItem = null
                                    showAddSheet = true
                                },
                                onEditItem = { item ->
                                    editingItem = item
                                    showAddSheet = true
                                }
                            )
                            MainTab.Profile -> ProfileComposeScreen(
                                onEditProfileClick = { isEditingProfile = true }
                            )
                        }
                    }

                    if (showAddSheet) {
                        AddEditFoodItemDialog(
                            viewModel = inventoryViewModel,
                            existingItem = editingItem,
                            onDismiss = {
                                showAddSheet = false
                                editingItem = null
                                inventoryViewModel.clearFormErrors()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo_utensils),
            contentDescription = "App logo",
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "dooF",
            modifier = Modifier.padding(start = 8.dp).weight(1f),
            color = colorResource(R.color.lime_primary),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.surface_container_high).copy(alpha = 0.6f))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_user_avatar),
                contentDescription = "User avatar",
                tint = colorResource(R.color.on_surface)
            )
        }
    }
}

@Composable
private fun MainBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    val containerColor = Color(0xFF0C1A10).copy(alpha = 0.9f)
    val activeColor = Color(0xFFD4FF00)
    val inactiveColor = Color(0xFFE8EAC8)
    val hoverColor = activeColor.copy(alpha = 0.16f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor, CircleShape)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.entries.forEach { tab ->
                MainBottomBarItem(
                    tab = tab,
                    selected = tab == selectedTab,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    hoverColor = hoverColor,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}


@Composable
private fun MainBottomBarItem(
    tab: MainTab,
    selected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    hoverColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) activeColor else Color.Transparent,
        label = "nav_bg_color"
    )

    val contentColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) Color(0xFF0C1A10) else inactiveColor,
        label = "nav_content_color"
    )
    
    val horizontalPadding by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (selected) 28.dp else 16.dp,
        label = "nav_padding"
    )

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = if (selected) tab.selectedIconRes else tab.unselectedIconRes
                ),
                contentDescription = tab.title,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = tab.title,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
