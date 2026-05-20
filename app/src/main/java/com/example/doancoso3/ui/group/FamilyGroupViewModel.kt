package com.example.doancoso3.ui.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancoso3.data.model.FamilyGroupEntity
import com.example.doancoso3.data.model.UserEntity
import com.example.doancoso3.data.repository.FamilyGroupRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyGroupViewModel @Inject constructor(
    private val repository: FamilyGroupRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _actionState = MutableLiveData<FamilyGroupActionState>(FamilyGroupActionState.Idle)
    val actionState: LiveData<FamilyGroupActionState> = _actionState

    private val _currentGroup = MutableLiveData<FamilyGroupEntity?>()
    val currentGroup: LiveData<FamilyGroupEntity?> = _currentGroup

    private val _members = MutableLiveData<List<UserEntity>>(emptyList())
    val members: LiveData<List<UserEntity>> = _members

    private var membersJob: Job? = null
    private var lastGroupId: String? = null

    init {
        observeCurrentGroup()
    }

    fun createGroup() {
        val userId = currentUserId()
        if (userId == null) {
            _actionState.value = FamilyGroupActionState.Error("Bạn cần đăng nhập để tạo nhóm")
            return
        }
        _actionState.value = FamilyGroupActionState.Loading
        viewModelScope.launch {
            val result = repository.createGroup(userId)
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

    fun refreshMembers() {
        val groupId = _currentGroup.value?.id ?: return
        viewModelScope.launch {
            repository.refreshGroupMembers(groupId)
        }
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
}

sealed class FamilyGroupActionState {
    data object Idle : FamilyGroupActionState()
    data object Loading : FamilyGroupActionState()
    data class Success(val message: String) : FamilyGroupActionState()
    data class Error(val message: String) : FamilyGroupActionState()
}
