package com.example.doancoso3.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancoso3.R
import com.example.doancoso3.data.model.NotificationEntity
import com.example.doancoso3.data.model.UserEntity

@Composable
fun NotificationsCard(
    notifications: List<NotificationEntity>,
    onAccept: (NotificationEntity) -> Unit,
    onDecline: (NotificationEntity) -> Unit
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
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = colorResource(R.color.lime_primary),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notifications",
                    color = colorResource(R.color.on_surface),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = colorResource(R.color.outline_variant)
            )

            if (notifications.isEmpty()) {
                Text(
                    text = "No new notifications",
                    color = colorResource(R.color.on_surface_variant),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                notifications.forEach { notification ->
                    NotificationItem(
                        notification = notification,
                        onAccept = { onAccept(notification) },
                        onDecline = { onDecline(notification) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationEntity,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container_high).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar Placeholder (MC)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF3A3D32), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = notification.senderName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()
                    Text(
                        text = initials,
                        color = colorResource(R.color.on_surface_variant),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = notification.senderName,
                        color = colorResource(R.color.lime_primary),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = notification.message,
                        color = colorResource(R.color.on_surface_variant),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.lime_primary),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.height(36.dp).weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Accept", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onDecline,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colorResource(R.color.outline)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.height(36.dp).weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Decline", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                
                // Add a little weight spacer
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
    }
}

@Composable
fun FamilyMembersCard(
    members: List<UserEntity>,
    ownerId: String?,
    currentUserId: String?,
    onRemoveMember: (String) -> Unit,
    onInviteClick: () -> Unit
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
                    tint = colorResource(R.color.lime_primary),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Family Members",
                    color = colorResource(R.color.on_surface),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = Color(0xFF1B2117),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "${members.size} Active",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = colorResource(R.color.on_surface_variant),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = colorResource(R.color.outline_variant)
            )

            members.forEach { member ->
                MemberItem(
                    member = member,
                    isAdmin = member.id == ownerId,
                    canManage = currentUserId == ownerId && member.id != currentUserId,
                    onRemove = { onRemoveMember(member.id) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onInviteClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorResource(R.color.outline_variant)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorResource(R.color.on_surface)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colorResource(R.color.lime_primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Add Family Member", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun MemberItem(
    member: UserEntity,
    isAdmin: Boolean,
    canManage: Boolean,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF2C2F27), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val initials = member.displayName.split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(1)
                .joinToString("")
                .uppercase()
            Text(
                text = initials,
                color = colorResource(R.color.on_surface_variant),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.displayName.ifBlank { "User" },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = if (isAdmin) "Admin" else "Member",
                color = colorResource(R.color.on_surface_variant),
                fontSize = 14.sp
            )
        }

        if (canManage) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = colorResource(R.color.on_surface_variant)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(colorResource(R.color.surface_container_high))
                ) {
                    DropdownMenuItem(
                        text = { Text("Remove from Group", color = colorResource(R.color.error)) },
                        onClick = {
                            showMenu = false
                            onRemove()
                        }
                    )
                }
            }
        }
    }
}
