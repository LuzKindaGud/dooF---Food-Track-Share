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
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancoso3.R
import com.example.doancoso3.ui.home.HomeComposeScreen
import com.example.doancoso3.ui.fooditem.InventoryComposeScreen

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
fun MainComposeScreen() {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Home) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colorResource(R.color.surface),
        topBar = { MainTopBar() },
        bottomBar = {
            MainBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = colorResource(R.color.lime_primary),
                contentColor = colorResource(R.color.dark_forest)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = "Add item"
                )
            }
        }
    ) { innerPadding ->
        val backgroundGradient = Brush.verticalGradient(
            colors = listOf(
                colorResource(R.color.surface),
                colorResource(R.color.surface_container_low)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundGradient)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(180))
                },
                label = "MainContentFade"
            ) { targetTab ->
                when (targetTab) {
                    MainTab.Home -> HomeComposeScreen()
                    MainTab.Inventory -> InventoryComposeScreen()
                    MainTab.Profile -> PlaceholderScreen("Profile")
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
            .background(colorResource(R.color.surface))
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
                .background(colorResource(R.color.surface_container_high))
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
    val containerColor = Color(0xFF0C1A10)
    val activeColor = Color(0xFFD4FF00)
    val inactiveColor = Color(0xFFE8EAC8)
    val hoverColor = activeColor.copy(alpha = 0.16f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.surface))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
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

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = colorResource(R.color.on_surface)
        )
    }
}
