package com.example.doancoso3.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.doancoso3.data.model.NotificationEntity
import com.example.doancoso3.data.model.UserEntity
import com.example.doancoso3.ui.auth.AuthActivity
import com.example.doancoso3.ui.group.FamilyGroupActionState
import com.example.doancoso3.ui.group.FamilyGroupViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileComposeScreen(
    onEditProfileClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    groupViewModel: FamilyGroupViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.observeAsState()
    val pushNotificationsEnabled by viewModel.pushNotificationsEnabled.observeAsState(true)
    val familySyncEnabled by viewModel.familySyncEnabled.observeAsState(false)
    val isLoggedOut by viewModel.isLoggedOut.observeAsState(false)

    val members by groupViewModel.members.observeAsState(emptyList())
    val currentGroup by groupViewModel.currentGroup.observeAsState()
    val actionState by groupViewModel.actionState.observeAsState(FamilyGroupActionState.Idle)
    val notifications by groupViewModel.notifications.observeAsState(emptyList())

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showCreateSheet by remember { mutableStateOf(false) }
    var showJoinSheet by remember { mutableStateOf(false) }
    var showInviteSheet by remember { mutableStateOf(false) }

    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Auto-close sheet on success
    LaunchedEffect(actionState) {
        if (actionState is FamilyGroupActionState.Success) {
            if (showCreateSheet || showJoinSheet) {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showCreateSheet = false
                        showJoinSheet = false
                    }
                }
            }
        }
    }

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            val intent = Intent(context, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            }
        )
    }

    // Removed displayNotifications mock logic
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // ... (Header, Profile Card, etc. unchanged)
        // Header
        Text(
            text = "Account & Settings",
            color = colorResource(R.color.primary_fixed),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Manage your profile details and household preferences.",
            color = colorResource(R.color.on_surface_variant),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Card
        ProfileInfoCard(
            name = userProfile?.displayName ?: "Loading...",
            email = userProfile?.email ?: "...",
            isPrimary = true,
            onEditClick = onEditProfileClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Family Group Section
        if (userProfile?.familyId == null) {
            FamilyGroupCard(
                hasGroup = false,
                onCreateClick = {
                    groupViewModel.resetActionState()
                    showCreateSheet = true
                },
                onJoinClick = {
                    groupViewModel.resetActionState()
                    showJoinSheet = true
                }
            )
        } else {
            FamilyMembersCard(
                members = members,
                ownerId = currentGroup?.ownerId,
                currentUserId = userProfile?.id,
                onRemoveMember = { groupViewModel.removeMember(it) },
                onInviteClick = { showInviteSheet = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications Card is now a permanent widget for ALL users (even new accounts)
        NotificationsCard(
            notifications = notifications,
            onAccept = { groupViewModel.respondToJoinRequest(it.id, true) },
            onDecline = { groupViewModel.respondToJoinRequest(it.id, false) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App Preferences Card
        AppPreferencesCard(
            pushNotificationsEnabled = pushNotificationsEnabled,
            familySyncEnabled = familySyncEnabled,
            onPushToggle = { viewModel.togglePushNotifications(it) },
            onSyncToggle = { viewModel.toggleFamilySync(it) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Log Out Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { showLogoutDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Log out",
                        tint = colorResource(R.color.error),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out",
                        color = colorResource(R.color.error),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            sheetState = sheetState,
            containerColor = colorResource(R.color.surface_container_low),
            dragHandle = null
        ) {
            CreateGroupSheetContent(
                onConfirm = { name -> groupViewModel.createGroup(name) },
                onCancel = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showCreateSheet = false
                        }
                    }
                },
                isLoading = actionState is FamilyGroupActionState.Loading,
                error = if (actionState is FamilyGroupActionState.Error) (actionState as FamilyGroupActionState.Error).message else null
            )
        }
    }

    if (showJoinSheet) {
        ModalBottomSheet(
            onDismissRequest = { showJoinSheet = false },
            sheetState = sheetState,
            containerColor = colorResource(R.color.surface_container_low),
            dragHandle = null
        ) {
            JoinGroupSheetContent(
                onConfirm = { code -> groupViewModel.joinGroup(code) },
                onCancel = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showJoinSheet = false
                        }
                    }
                },
                isLoading = actionState is FamilyGroupActionState.Loading,
                error = if (actionState is FamilyGroupActionState.Error) (actionState as FamilyGroupActionState.Error).message else null
            )
        }
    }

    if (showInviteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInviteSheet = false },
            sheetState = sheetState,
            containerColor = colorResource(R.color.surface_container_low),
            dragHandle = null
        ) {
            InviteMemberSheetContent(
                familyId = userProfile?.familyId ?: "N/A",
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showInviteSheet = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
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
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false) { }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container_high))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(colorResource(R.color.error_container)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = colorResource(R.color.error),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Log Out",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.on_surface)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Are you sure you want to log out of your account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(R.color.on_surface_variant),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, colorResource(R.color.outline))
                        ) {
                            Text(
                                text = "Cancel",
                                color = colorResource(R.color.on_surface)
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.error),
                                contentColor = colorResource(R.color.on_error)
                            )
                        ) {
                            Text(
                                text = "Log Out",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(name: String, email: String, isPrimary: Boolean, onEditClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            Column {
                Text(
                    text = "My Profile",
                    color = colorResource(R.color.on_surface),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_user_avatar),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, colorResource(R.color.outline_variant), CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = name,
                            color = colorResource(R.color.on_surface),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = email,
                            color = colorResource(R.color.on_surface_variant),
                            fontSize = 14.sp
                        )
                        
                        if (isPrimary) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        colorResource(R.color.surface_variant),
                                        RoundedCornerShape(999.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "PRIMARY ACCOUNT",
                                    color = colorResource(R.color.on_surface),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.05.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.primary_container),
                        contentColor = colorResource(R.color.on_primary_container)
                    ),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Profile",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateGroupSheetContent(
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean,
    error: String? = null
) {
    var groupName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.surface_container_low))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(width = 40.dp, height = 4.dp)
                .background(colorResource(R.color.outline_variant), CircleShape)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Icon with lime tint
        Icon(
            imageVector = Icons.Default.GroupAdd,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = colorResource(R.color.lime_primary)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Create New Family Group",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start sharing your food inventory and shopping lists with your loved ones.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(R.color.on_surface_variant),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        if (error != null) {
            Text(
                text = error,
                color = colorResource(R.color.error),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Label and TextField
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "GROUP NAME",
                style = MaterialTheme.typography.labelLarge,
                color = colorResource(R.color.lime_primary),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                placeholder = { 
                    Text(
                        "e.g., The Jenkins Family", 
                        color = colorResource(R.color.on_surface_variant).copy(alpha = 0.5f)
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = colorResource(R.color.surface_container_high),
                    unfocusedContainerColor = colorResource(R.color.surface_container_high),
                    focusedBorderColor = colorResource(R.color.outline_variant),
                    unfocusedBorderColor = colorResource(R.color.outline_variant),
                    cursorColor = colorResource(R.color.lime_primary)
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Family Action Button (LIME)
        Button(
            onClick = { 
                if (groupName.isNotBlank() && !isLoading) {
                    onConfirm(groupName)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.lime_primary),
                contentColor = Color.Black,
                disabledContainerColor = colorResource(R.color.lime_primary).copy(alpha = 0.3f),
                disabledContentColor = Color.Black.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = groupName.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "CREATE GROUP NOW",
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cancel Text Button
        Text(
            text = "Cancel",
            modifier = Modifier
                .clickable(enabled = !isLoading) { onCancel() }
                .padding(12.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun JoinGroupSheetContent(
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean,
    error: String? = null
) {
    var familyId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.surface_container_low))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(width = 40.dp, height = 4.dp)
                .background(colorResource(R.color.outline_variant), CircleShape)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Icon with lime tint
        Icon(
            imageVector = Icons.Default.Login,
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(CircleShape),
            tint = colorResource(R.color.lime_primary)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Join a Family Group",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter the 6-character Family ID provided by the group owner to start sharing data.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(R.color.on_surface_variant),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        if (error != null) {
            Text(
                text = error,
                color = colorResource(R.color.error),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Label and TextField
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "FAMILY ID",
                style = MaterialTheme.typography.labelLarge,
                color = colorResource(R.color.lime_primary),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = familyId,
                onValueChange = { if (it.length <= 6) familyId = it.uppercase() },
                placeholder = { 
                    Text(
                        "E.G., AB12CD", 
                        color = colorResource(R.color.on_surface_variant).copy(alpha = 0.5f)
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = colorResource(R.color.surface_container_high),
                    unfocusedContainerColor = colorResource(R.color.surface_container_high),
                    focusedBorderColor = colorResource(R.color.outline_variant),
                    unfocusedBorderColor = colorResource(R.color.outline_variant),
                    cursorColor = colorResource(R.color.lime_primary)
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Button
        Button(
            onClick = { 
                if (familyId.length == 6 && !isLoading) {
                    onConfirm(familyId)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.lime_primary),
                contentColor = Color.Black,
                disabledContainerColor = colorResource(R.color.lime_primary).copy(alpha = 0.3f),
                disabledContentColor = Color.Black.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = familyId.length == 6 && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "JOIN GROUP NOW",
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cancel
        Text(
            text = "Cancel",
            modifier = Modifier
                .clickable(enabled = !isLoading) { onCancel() }
                .padding(12.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Note: You can only be a member of one group at a time.",
            style = MaterialTheme.typography.bodySmall,
            color = colorResource(R.color.on_surface_variant),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun InviteMemberSheetContent(
    familyId: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.surface_container_low))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Custom Drag Handle
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 4.dp)
                .background(colorResource(R.color.outline_variant), CircleShape)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Icon with lime tint
        Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = colorResource(R.color.lime_primary)
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Invite Your Family",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Chia sẻ mã ID duy nhất này với những người bạn muốn mời vào nhóm gia đình.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(R.color.on_surface_variant),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Family ID Box with Copy Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            color = colorResource(R.color.surface_container_high),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = familyId,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorResource(R.color.lime_primary),
                    letterSpacing = 2.sp
                )
                
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Family ID", familyId)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy ID",
                        tint = colorResource(R.color.on_surface_variant),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // SHARE INVITE LINK Button
        Button(
            onClick = { 
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Join my food inventory group on Food-Track-Share! My Family ID is: $familyId")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Invite via"))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.lime_primary),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SHARE INVITE LINK",
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Done Text Button
        Text(
            text = "Done",
            modifier = Modifier
                .clickable { onClose() }
                .padding(12.dp),
            color = colorResource(R.color.on_surface_variant),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FamilyGroupCard(
    hasGroup: Boolean, 
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = colorResource(R.color.primary_fixed),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Family Group",
                    color = colorResource(R.color.on_surface),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = colorResource(R.color.outline_variant)
            )

            if (!hasGroup) {
                Text(
                    text = "You are not currently part of a family group. Join one to share inventory and track food together.",
                    color = colorResource(R.color.on_surface_variant),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onCreateClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.primary_container),
                        contentColor = colorResource(R.color.on_primary_container)
                    ),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Create a New Group", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onJoinClick,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(2.dp, colorResource(R.color.outline_variant)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colorResource(R.color.on_surface)),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Join a Group", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AppPreferencesCard(
    pushNotificationsEnabled: Boolean,
    familySyncEnabled: Boolean,
    onPushToggle: (Boolean) -> Unit,
    onSyncToggle: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = colorResource(R.color.primary_fixed),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Preferences",
                    color = colorResource(R.color.on_surface),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = colorResource(R.color.outline_variant)
            )

            PreferenceToggle(
                title = "Push Notifications",
                description = "Receive alerts for expiring food and shared updates.",
                checked = pushNotificationsEnabled,
                onCheckedChange = onPushToggle
            )

            Spacer(modifier = Modifier.height(24.dp))

            PreferenceToggle(
                title = "Family Sync",
                description = "Automatically share your inventory additions with the household.",
                checked = familySyncEnabled,
                onCheckedChange = onSyncToggle
            )
        }
    }
}

@Composable
private fun PreferenceToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colorResource(R.color.on_surface),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = colorResource(R.color.on_surface_variant),
                fontSize = 14.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(R.color.on_primary_container),
                checkedTrackColor = colorResource(R.color.primary_container),
                uncheckedThumbColor = colorResource(R.color.on_surface),
                uncheckedTrackColor = colorResource(R.color.surface_variant),
                uncheckedBorderColor = colorResource(R.color.outline_variant)
            )
        )
    }
}

// Helper for OutlinedButton border to use Compose UI Modifier
@Composable
private fun border(width: androidx.compose.ui.unit.Dp, color: androidx.compose.ui.graphics.Color) = 
    androidx.compose.foundation.BorderStroke(width, color)
