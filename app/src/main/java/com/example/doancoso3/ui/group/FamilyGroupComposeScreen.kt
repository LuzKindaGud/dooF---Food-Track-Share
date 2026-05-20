package com.example.doancoso3.ui.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doancoso3.R
import com.example.doancoso3.data.model.UserEntity

@Composable
fun FamilyGroupComposeScreen(viewModel: FamilyGroupViewModel = hiltViewModel()) {
    val currentGroup by viewModel.currentGroup.observeAsState()
    val members by viewModel.members.observeAsState(emptyList())
    val actionState by viewModel.actionState.observeAsState(FamilyGroupActionState.Idle)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Family Group",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        when (actionState) {
            is FamilyGroupActionState.Error -> {
                Text(
                    text = (actionState as FamilyGroupActionState.Error).message,
                    color = colorResource(R.color.error),
                    fontSize = 13.sp
                )
            }
            is FamilyGroupActionState.Success -> {
                Text(
                    text = (actionState as FamilyGroupActionState.Success).message,
                    color = colorResource(R.color.lime_primary),
                    fontSize = 13.sp
                )
            }
            else -> Unit
        }

        if (currentGroup == null) {
            CreateGroupCard(onCreate = viewModel::createGroup)
            JoinGroupCard(onJoin = viewModel::joinGroup)
        } else {
            GroupSummaryCard(
                groupId = currentGroup?.id.orEmpty(),
                isOwner = currentGroup?.ownerId == viewModel.currentUserId(),
                onLeave = viewModel::leaveGroup
            )
            GroupMembersCard(
                members = members,
                canRemove = currentGroup?.ownerId == viewModel.currentUserId(),
                onRemove = viewModel::removeMember
            )
        }
    }
}

@Composable
private fun CreateGroupCard(onCreate: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container_high)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tạo nhóm mới",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Tạo Family ID để mời thành viên.",
                fontSize = 13.sp,
                color = colorResource(R.color.on_surface_variant),
                modifier = Modifier.padding(top = 4.dp)
            )
            Button(
                onClick = onCreate,
                modifier = Modifier.padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.lime_primary),
                    contentColor = colorResource(R.color.button_text_dark)
                )
            ) {
                Text(text = "Create Group")
            }
        }
    }
}

@Composable
private fun JoinGroupCard(onJoin: (String) -> Unit) {
    var familyId by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container_high)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tham gia nhóm",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            OutlinedTextField(
                value = familyId,
                onValueChange = { familyId = it },
                label = { Text("Family ID") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )
            Button(
                onClick = { onJoin(familyId) },
                modifier = Modifier.padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.lime_primary),
                    contentColor = colorResource(R.color.button_text_dark)
                )
            ) {
                Text(text = "Join Group")
            }
        }
    }
}

@Composable
private fun GroupSummaryCard(
    groupId: String,
    isOwner: Boolean,
    onLeave: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container_high)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Family ID: $groupId",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = if (isOwner) "Bạn là chủ nhóm" else "Bạn là thành viên",
                fontSize = 13.sp,
                color = colorResource(R.color.on_surface_variant),
                modifier = Modifier.padding(top = 4.dp)
            )
            Button(
                onClick = onLeave,
                modifier = Modifier.padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.error),
                    contentColor = colorResource(R.color.on_error)
                )
            ) {
                Text(text = "Leave Group")
            }
        }
    }
}

@Composable
private fun GroupMembersCard(
    members: List<UserEntity>,
    canRemove: Boolean,
    onRemove: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.surface_container_high)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Thành viên (${members.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (members.isEmpty()) {
                Text(
                    text = "Chưa có thành viên nào.",
                    fontSize = 13.sp,
                    color = colorResource(R.color.on_surface_variant)
                )
                return@Column
            }
            LazyColumn {
                items(members) { member ->
                    MemberRow(member = member, canRemove = canRemove, onRemove = onRemove)
                    HorizontalDivider(color = colorResource(R.color.outline_variant))
                }
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: UserEntity,
    canRemove: Boolean,
    onRemove: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.displayName.ifBlank { "User" },
                fontWeight = FontWeight.Bold
            )
            Text(
                text = member.email,
                fontSize = 12.sp,
                color = colorResource(R.color.on_surface_variant)
            )
        }
        if (canRemove) {
            Button(
                onClick = { onRemove(member.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.surface_container),
                    contentColor = colorResource(R.color.on_surface)
                )
            ) {
                Text(text = "Remove")
            }
        }
    }
}
