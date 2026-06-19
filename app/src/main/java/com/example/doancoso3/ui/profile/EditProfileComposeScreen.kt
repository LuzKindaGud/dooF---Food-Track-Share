package com.example.doancoso3.ui.profile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doancoso3.R
import com.example.doancoso3.ui.components.FreshVitalityBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileComposeScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.observeAsState()
    val updateState by viewModel.updateState.observeAsState(ProfileUpdateState.Idle)
    val resetPasswordState by viewModel.resetPasswordState.observeAsState(ProfileUpdateState.Idle)
    val context = LocalContext.current

    var displayName by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    
    // Initialize displayName when profile is loaded
    LaunchedEffect(userProfile) {
        userProfile?.let {
            if (displayName.isEmpty()) {
                displayName = it.displayName
            }
        }
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is ProfileUpdateState.Success -> {
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
                onBack()
            }
            is ProfileUpdateState.Error -> {
                Toast.makeText(context, (updateState as ProfileUpdateState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    if (showResetDialog) {
        ResetPasswordDialog(
            email = userProfile?.email ?: "",
            state = resetPasswordState,
            onDismiss = { 
                showResetDialog = false 
                viewModel.resetUpdateState()
            },
            onConfirm = { viewModel.resetPassword() }
        )
    }

    FreshVitalityBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Edit Profile",
                            color = colorResource(R.color.primary_fixed),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colorResource(R.color.primary_fixed)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Profile Image Section
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_user_avatar),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFFD4FF00), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        color = Color(0xFFD4FF00),
                        onClick = { /* TODO: Change photo */ }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Photo",
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF01180A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Main Content Card (Based on JSON)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0A170F)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // FULL NAME Section
                        UserField(
                            label = "FULL NAME",
                            value = displayName,
                            onValueChange = { displayName = it },
                            icon = Icons.Outlined.Person
                        )

                        // EMAIL ADDRESS Section
                        UserField(
                            label = "EMAIL ADDRESS",
                            value = userProfile?.email ?: "...",
                            onValueChange = { },
                            icon = Icons.Outlined.Email,
                            enabled = false,
                            iconColor = Color(0xFF8B9E8A)
                        )

                        // PASSWORD Section (Action Button)
                        Column {
                            Text(
                                text = "PASSWORD",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF8B9E8A),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                onClick = { showResetDialog = true },
                                color = Color(0xFF112216),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFFDAB770),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Text(
                                        text = "Reset Password",
                                        color = Color(0xFFEAEAE2),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color(0xFFDAB770),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Save Button (Vibrant Lime)
                Button(
                    onClick = { 
                        if (displayName.isNotBlank()) {
                            viewModel.updateProfile(displayName)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4FF00),
                        contentColor = Color(0xFF01180A)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    enabled = updateState !is ProfileUpdateState.Loading && displayName.isNotBlank()
                ) {
                    if (updateState is ProfileUpdateState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF01180A),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "SAVE CHANGES",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ResetPasswordDialog(
    email: String,
    state: ProfileUpdateState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(enabled = false) { }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A170F)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF112216))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Visual Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                if (state is ProfileUpdateState.Success) Color(0xFFD4FF00).copy(alpha = 0.1f)
                                else Color(0xFFDAB770).copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state is ProfileUpdateState.Success) Icons.Default.Check else Icons.Default.Email,
                            contentDescription = null,
                            tint = if (state is ProfileUpdateState.Success) Color(0xFFD4FF00) else Color(0xFFDAB770),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = if (state is ProfileUpdateState.Success) "Email Sent!" else "Reset Password",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = when (state) {
                            is ProfileUpdateState.Success -> "Chúng tôi đã gửi liên kết khôi phục đến:\n$email"
                            is ProfileUpdateState.Error -> "Lỗi: ${state.message}"
                            else -> "Chúng tôi sẽ gửi một liên kết khôi phục mật khẩu đến email của bạn. Vui lòng kiểm tra hộp thư đến."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B9E8A),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (state is ProfileUpdateState.Success) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4FF00), contentColor = Color(0xFF01180A)),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("GREAT, THANKS!", fontWeight = FontWeight.ExtraBold)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onConfirm,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD4FF00),
                                    contentColor = Color(0xFF01180A)
                                ),
                                shape = RoundedCornerShape(28.dp),
                                enabled = state !is ProfileUpdateState.Loading
                            ) {
                                if (state is ProfileUpdateState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF01180A), strokeWidth = 2.dp)
                                } else {
                                    Text("SEND RECOVERY LINK", fontWeight = FontWeight.ExtraBold)
                                }
                            }

                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("NOT NOW", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    enabled: Boolean = true,
    iconColor: Color = Color(0xFFE2EAE2)
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF8B9E8A),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            enabled = enabled,
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF112216),
                unfocusedContainerColor = Color(0xFF112216),
                disabledContainerColor = Color(0xFF112216).copy(alpha = 0.8f),
                focusedTextColor = Color(0xFFE2EAE2),
                unfocusedTextColor = Color(0xFFE2EAE2),
                disabledTextColor = Color(0xFFE2EAE2).copy(alpha = 0.6f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFFD4FF00)
            ),
            singleLine = true
        )
    }
}
