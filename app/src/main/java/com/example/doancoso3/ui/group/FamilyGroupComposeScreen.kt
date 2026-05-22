package com.example.doancoso3.ui.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doancoso3.R
import com.example.doancoso3.data.model.UserEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyGroupComposeScreen(viewModel: FamilyGroupViewModel = hiltViewModel()) {
    val currentGroup by viewModel.currentGroup.observeAsState()
    val members by viewModel.members.observeAsState(emptyList())
    val actionState by viewModel.actionState.observeAsState(FamilyGroupActionState.Idle)

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showCreateSheet by remember { mutableStateOf(false) }

    // Tự động đóng BottomSheet khi tạo thành công
    LaunchedEffect(actionState) {
        if (actionState is FamilyGroupActionState.Success && showCreateSheet) {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    showCreateSheet = false
                }
            }
        }
    }

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
            CreateGroupCard(onOpenCreate = {
                viewModel.resetActionState()
                showCreateSheet = true
            })
            JoinGroupCard(onJoin = viewModel::joinGroup)
        } else {
            GroupSummaryCard(
                groupName = currentGroup?.name ?: "Family Group",
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

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            sheetState = sheetState,
            containerColor = colorResource(R.color.surface_container_low),
            dragHandle = { /* Custom drag handle if needed */ }
        ) {
            CreateGroupSheetContent(
                onConfirm = { name -> viewModel.createGroup(name) },
                onCancel = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showCreateSheet = false
                        }
                    }
                },
                isLoading = actionState is FamilyGroupActionState.Loading
            )
        }
    }
}

@Composable
private fun CreateGroupSheetContent(
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean
) {
    var groupName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_family_group),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tạo Nhóm Gia Đình Mới",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Bắt đầu chia sẻ kho thực phẩm và danh sách mua sắm với những người thân yêu.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(R.color.on_surface_variant),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text("Tên nhóm (ví dụ: Nhà họ Nguyễn)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onConfirm(groupName) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.lime_primary),
                contentColor = colorResource(R.color.button_text_dark)
            ),
            enabled = groupName.isNotBlank() && !isLoading
        ) {
            Text(text = if (isLoading) "Đang tạo..." else "Tạo Nhóm Ngay")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.surface_container_high),
                contentColor = colorResource(R.color.on_surface)
            ),
            enabled = !isLoading
        ) {
            Text(text = "Hủy bỏ")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CreateGroupCard(onOpenCreate: () -> Unit) {
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
                onClick = onOpenCreate,
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
    groupName: String,
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
                text = groupName,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = "Family ID: $groupId",
                fontSize = 14.sp,
                color = colorResource(R.color.on_surface_variant)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isOwner) "Bạn là chủ nhóm" else "Bạn là thành viên",
                fontSize = 13.sp,
                color = colorResource(R.color.on_surface_variant)
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
            } else {
                members.forEach { member ->
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
