package com.jagdishsports.gymswimming.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class MemberRepository private constructor(
    private val memberDao: MemberDao
) {
    fun observeAllMembers(): Flow<List<MemberEntity>> = memberDao.observeAll()

    fun observeMembersByCategory(category: String): Flow<List<MemberEntity>> {
        return memberDao.observeByCategory(category)
    }

    fun observeMember(id: Long): Flow<MemberEntity?> = memberDao.observeById(id)

    suspend fun saveMember(member: MemberEntity) {
        if (member.id == 0L) {
            memberDao.insert(member)
        } else {
            memberDao.update(member)
        }
    }

    suspend fun deleteMember(member: MemberEntity) {
        memberDao.delete(member)
    }

    suspend fun deleteMemberById(id: Long) {
        memberDao.deleteById(id)
    }

    suspend fun getMembersExpiringBetween(
        startEpochDay: Long,
        endEpochDay: Long
    ): List<MemberEntity> {
        return memberDao.getExpiringBetween(startEpochDay, endEpochDay)
    }

    companion object {
        @Volatile
        private var instance: MemberRepository? = null

        fun getInstance(context: Context): MemberRepository {
            return instance ?: synchronized(this) {
                instance ?: MemberRepository(
                    AppDatabase.getInstance(context).memberDao()
                ).also { instance = it }
            }
        }
    }
}
