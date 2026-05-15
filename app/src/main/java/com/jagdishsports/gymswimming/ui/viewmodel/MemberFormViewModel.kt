package com.jagdishsports.gymswimming.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.jagdishsports.gymswimming.data.MemberEntity
import com.jagdishsports.gymswimming.data.MemberRepository
import kotlinx.coroutines.flow.Flow

class MemberFormViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MemberRepository.getInstance(application)

    fun observeMember(id: Long): Flow<MemberEntity?> = repository.observeMember(id)

    suspend fun save(member: MemberEntity) {
        repository.saveMember(member)
    }

    suspend fun delete(member: MemberEntity) {
        repository.deleteMember(member)
    }
}
