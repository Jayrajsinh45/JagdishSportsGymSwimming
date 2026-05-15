package com.jagdishsports.gymswimming.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jagdishsports.gymswimming.data.MemberEntity
import com.jagdishsports.gymswimming.data.MemberRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class CategoryMembersViewModel(
    application: Application,
    category: String
) : AndroidViewModel(application) {
    private val repository = MemberRepository.getInstance(application)

    val members: StateFlow<List<MemberEntity>> = repository
        .observeMembersByCategory(category)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun delete(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }
}
