package com.example.doancoso3.ui.group

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancoso3.data.model.FamilyGroupEntity
import com.example.doancoso3.data.model.NotificationEntity
import com.example.doancoso3.data.model.UserEntity
import com.example.doancoso3.data.repository.FamilyGroupRepository
import com.example.doancoso3.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyGroupViewModel @Inject constructor(
    private val repository: FamilyGroupRepository,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _actionState = MutableLiveData<FamilyGroupActionState>(FamilyGroupActionState.Idle)
    val actionState: LiveData<FamilyGroupActionState> = _actionState

    private val _currentGroup = MutableLiveData<FamilyGroupEntity?>()
    val currentGroup: LiveData<FamilyGroupEntity?> = _currentGroup

    private val _members = MutableLiveData<List<UserEntity>>(emptyList())
    val members: LiveData<List<UserEntity>> = _members

    private val _notifications = MutableLiveData<List<NotificationEntity>>(emptyList())
    val notifications: LiveData<List<NotificationEntity>> = _notifications

    private var membersJob: Job? = null
    private var notificationsJob: Job? = null
    private var lastGroupId: String? = null
    private var lastNotificationIds = mutableSetOf<String>()

    init {
        observeCurrentGroup()
        observeNotifications()
    }

    fun createGroup(name: String) {
        val userId = currentUserId()
        if (userId == null) {
            _actionState.value = FamilyGroupActionState.Error("Bạn cần đăng nhập để tạo nhóm")
            return
        }
        if (name.isBlank()) {
            _actionState.value = FamilyGroupActionState.Error("Tên nhóm không được để trống")
            return
        }

        _actionState.value = FamilyGroupActionState.Loading
        viewModelScope.launch {
            val result = repository.createGroup(userId, name)
            result.fold(
                onSuccess = {
                    _actionState.value = FamilyGroupActionState.Success("Tạo nhóm thành công")
                },
                onFailure = {
                    _actionState.value = FamilyGroupActionState.Error(it.message ?: "Tạo nhóm thất bại")
                }
            )
        }
    }

    fun joinGroup(familyId: String) {
        val userId = currentUserId()
        if (userId == null) {
            _actionState.value = FamilyGroupActionState.Error("Bạn cần đăng nhập để tham gia nhóm")
            return
        }
        _actionState.value = FamilyGroupActionState.Loading
        viewModelScope.launch {
            val result = repository.joinGroup(userId, familyId)
            result.fold(
                onSuccess = {
                    _actionState.value = FamilyGroupActionState.Success("Tham gia nhóm thành công")
                },
                onFailure = {
                    _actionState.value = FamilyGroupActionState.Error(it.message ?: "Tham gia nhóm thất bại")
                }
            )
        }
    }

    fun leaveGroup() {
        val userId = currentUserId()
        if (userId == null) {
            _actionState.value = FamilyGroupActionState.Error("Bạn cần đăng nhập để rời nhóm")
            return
        }
        _actionState.value = FamilyGroupActionState.Loading
        viewModelScope.launch {
            val result = repository.leaveGroup(userId)
            result.fold(
                onSuccess = {
                    _actionState.value = FamilyGroupActionState.Success("Đã rời nhóm")
                },
                onFailure = {
                    _actionState.value = FamilyGroupActionState.Error(it.message ?: "Rời nhóm thất bại")
                }
            )
        }
    }

    fun removeMember(memberId: String) {
        val ownerId = currentUserId()
        if (ownerId == null) {
            _actionState.value = FamilyGroupActionState.Error("Bạn cần đăng nhập để xóa thành viên")
            return
        }
        _actionState.value = FamilyGroupActionState.Loading
        viewModelScope.launch {
            val result = repository.removeMember(ownerId, memberId)
            result.fold(
                onSuccess = {
                    _actionState.value = FamilyGroupActionState.Success("Đã xóa thành viên")
                },
                onFailure = {
                    _actionState.value = FamilyGroupActionState.Error(it.message ?: "Xóa thành viên thất bại")
                }
            )
        }
    }

    fun respondToJoinRequest(notificationId: String, accept: Boolean) {
        _actionState.value = FamilyGroupActionState.Loading
        viewModelScope.launch {
            val result = repository.respondToJoinRequest(notificationId, accept)
            result.fold(
                onSuccess = {
                    val msg = if (accept) "Đã chấp nhận yêu cầu" else "Đã từ chối yêu cầu"
                    _actionState.value = FamilyGroupActionState.Success(msg)
                },
                onFailure = {
                    _actionState.value = FamilyGroupActionState.Error(it.message ?: "Thao tác thất bại")
                }
            )
        }
    }

    fun refreshMembers() {
        val groupId = _currentGroup.value?.id ?: return
        viewModelScope.launch {
            repository.refreshGroupMembers(groupId)
        }
    }

    fun resetActionState() {
        _actionState.value = FamilyGroupActionState.Idle
    }

    fun currentUserId(): String? = firebaseAuth.currentUser?.uid

    private fun observeCurrentGroup() {
        val userId = currentUserId() ?: return
        viewModelScope.launch {
            repository.getUserGroup(userId).collectLatest { group ->
                _currentGroup.postValue(group)
                if (group?.id != null && group.id != lastGroupId) {
                    lastGroupId = group.id
                    observeMembers(group.id)
                    viewModelScope.launch {
                        repository.refreshGroupMembers(group.id)
                    }
                }
            }
        }
    }

    private fun observeMembers(groupId: String) {
        membersJob?.cancel()
        membersJob = viewModelScope.launch {
            repository.getGroupMembers(groupId).collectLatest { users ->
                _members.postValue(users)
            }
        }
    }

    private fun observeNotifications() {
        val userId = currentUserId() ?: return
        notificationsJob?.cancel()
        notificationsJob = viewModelScope.launch {
            repository.observeNotifications(userId).collectLatest { list ->
                // Filter only PENDING join requests to ensure they disappear after action
                val pendingRequests = list.filter { it.type == "JOIN_REQUEST" && it.status == "PENDING" }
                
                // Trigger local notification for new requests
                pendingRequests.forEach { noti ->
                    if (!lastNotificationIds.contains(noti.id)) {
                        NotificationHelper.showJoinRequestNotification(context, noti.senderName)
                        lastNotificationIds.add(noti.id)
                    }
                }
                
                _notifications.postValue(pendingRequests)
            }
        }
    }
}

sealed class FamilyGroupActionState {
    data object Idle : FamilyGroupActionState()
    data object Loading : FamilyGroupActionState()
    data class Success(val message: String) : FamilyGroupActionState()
    data class Error(val message: String) : FamilyGroupActionState()
}
