package com.jagdishsports.gymswimming.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jagdishsports.gymswimming.data.MemberEntity
import com.jagdishsports.gymswimming.data.MemberRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MemberRepository.getInstance(application)

    val members: StateFlow<List<MemberEntity>> = repository
        .observeAllMembers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
