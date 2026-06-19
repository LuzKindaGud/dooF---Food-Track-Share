package com.example.doancoso3.ui.fooditem

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancoso3.R
import com.example.doancoso3.data.model.FoodItemEntity
import com.example.doancoso3.data.model.StorageLocation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
private val UNIT_OPTIONS = listOf("pcs", "g", "kg", "L", "ml", "pack")
private val QUICK_EXPIRY = listOf("3 days" to 3, "1 week" to 7, "2 weeks" to 14)

/**
 * "Confirm Item Details" sheet that slides up from the bottom (Add / Edit food item).
 * Saving is delegated to [FoodItemViewModel.saveFoodItem], which writes through the
 * repository (Room first, then Firestore sync) following the project's MVVM pattern.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodItemDialog(
    viewModel: FoodItemViewModel,
    existingItem: FoodItemEntity?,
    onDismiss: () -> Unit
) {
    val formErrors by viewModel.formErrors.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var quantity by remember { mutableStateOf((existingItem?.quantity?.toInt() ?: 1).coerceAtLeast(1)) }
    var unit by remember { mutableStateOf(existingItem?.unit ?: "pcs") }
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var location by remember {
        mutableStateOf(
            existingItem?.storageLocation
                ?.let { runCatching { StorageLocation.valueOf(it) }.getOrNull() }
                ?: StorageLocation.FRIDGE
        )
    }
    var expiryDate by remember { mutableStateOf(existingItem?.expiryDate) }
    var selectedQuick by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.clearFormErrors() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(R.color.surface),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Lime top accent
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.primary_fixed_dim).copy(alpha = 0.4f))
            )

            Spacer(Modifier.height(16.dp))

            // Title
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (existingItem == null) "Confirm Item Details" else "Edit Item",
                    color = colorResource(R.color.on_surface),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colorResource(R.color.on_surface_variant),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { onDismiss() }
                )
            }

            Spacer(Modifier.height(24.dp))

            // Item identity: image placeholder + camera badge
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colorResource(R.color.surface_container_highest))
                        .border(2.dp, colorResource(R.color.primary_fixed_dim), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = colorResource(R.color.on_surface_variant),
                        modifier = Modifier.size(48.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.primary_fixed)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Add photo",
                        tint = colorResource(R.color.on_primary_fixed),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Item name (underlined field)
            FieldLabel("Item Name")
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = colorResource(R.color.on_surface),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(colorResource(R.color.primary_fixed)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            HorizontalDivider(
                color = if (formErrors.nameError != null) colorResource(R.color.error)
                else colorResource(R.color.primary_fixed_dim)
            )
            ErrorText(formErrors.nameError)

            Spacer(Modifier.height(24.dp))

            // Storage location
            FieldLabel("Storage Location")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StorageLocation.entries.forEach { loc ->
                    StorageButton(
                        label = loc.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = loc == location,
                        onClick = { location = loc },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Quantity & unit
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("Quantity")
                    Spacer(Modifier.height(8.dp))
                    QuantityStepper(
                        value = quantity,
                        onDecrease = { if (quantity > 1) quantity-- },
                        onIncrease = { quantity++ }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("Unit")
                    Spacer(Modifier.height(8.dp))
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(CircleShape)
                                .background(colorResource(R.color.surface_container_high))
                                .clickable { unitMenuExpanded = true }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = unit,
                                color = colorResource(R.color.on_surface),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = colorResource(R.color.on_surface),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = unitMenuExpanded,
                            onDismissRequest = { unitMenuExpanded = false }
                        ) {
                            UNIT_OPTIONS.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        unitMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            ErrorText(formErrors.quantityError)

            Spacer(Modifier.height(24.dp))

            // Quick expiry
            FieldLabel("Quick Expiry")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QUICK_EXPIRY.forEach { (label, days) ->
                    ExpiryChip(
                        label = label,
                        selected = selectedQuick == label,
                        onClick = {
                            selectedQuick = label
                            expiryDate = System.currentTimeMillis() + days * DAY_MILLIS
                        }
                    )
                }
                ExpiryChip(
                    label = customLabel(selectedQuick, expiryDate),
                    selected = selectedQuick == "Custom",
                    leadingIcon = Icons.Default.CalendarToday,
                    onClick = { showDatePicker = true }
                )
            }
            ErrorText(formErrors.expiryError)

            Spacer(Modifier.height(28.dp))

            // Add to inventory
            Button(
                onClick = {
                    viewModel.saveFoodItem(
                        existingItem = existingItem,
                        name = name,
                        quantityText = quantity.toString(),
                        expiryDate = expiryDate,
                        location = location,
                        barcode = existingItem?.barcode,
                        unit = unit
                    )
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.primary_fixed),
                    contentColor = colorResource(R.color.on_primary_fixed)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colorResource(R.color.on_primary_fixed),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (existingItem == null) "Add to Inventory" else "Save Changes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (existingItem != null) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { viewModel.deleteItem(existingItem.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = colorResource(R.color.error),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Remove Item", color = colorResource(R.color.error))
                }
            } else {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = colorResource(R.color.on_surface_variant))
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    expiryDate = datePickerState.selectedDateMillis
                    selectedQuick = "Custom"
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = colorResource(R.color.primary_fixed_dim),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun ErrorText(error: String?) {
    if (error != null) {
        Text(
            text = error,
            color = colorResource(R.color.error),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun StorageButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) colorResource(R.color.primary_container)
                else Color.Transparent
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colorResource(R.color.primary_fixed_dim)
                else colorResource(R.color.outline_variant),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) colorResource(R.color.on_primary_container)
            else colorResource(R.color.on_surface_variant),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun QuantityStepper(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(CircleShape)
            .background(colorResource(R.color.surface_container_high))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.surface_container_highest))
                .clickable { onDecrease() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Decrease",
                tint = colorResource(R.color.on_surface)
            )
        }
        Text(
            text = value.toString(),
            color = colorResource(R.color.on_surface),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.primary_fixed))
                .clickable { onIncrease() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Increase",
                tint = colorResource(R.color.on_primary_fixed)
            )
        }
    }
}

@Composable
private fun ExpiryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (selected) colorResource(R.color.primary_container)
                else colorResource(R.color.surface_container_highest)
            )
            .border(
                1.dp,
                if (selected) colorResource(R.color.primary_fixed_dim)
                else colorResource(R.color.outline_variant),
                CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = if (selected) colorResource(R.color.on_primary_container)
                else colorResource(R.color.on_surface),
                modifier = Modifier
                    .size(14.dp)
                    .padding(end = 0.dp)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = label,
            color = if (selected) colorResource(R.color.on_primary_container)
            else colorResource(R.color.on_surface),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun customLabel(selectedQuick: String?, expiryDate: Long?): String {
    return if (selectedQuick == "Custom" && expiryDate != null) {
        SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(expiryDate))
    } else {
        "Custom"
    }
}
