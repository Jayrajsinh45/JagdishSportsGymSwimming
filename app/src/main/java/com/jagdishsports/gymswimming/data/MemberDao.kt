package com.jagdishsports.gymswimming.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY endDateEpochDay ASC, fullName COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE category = :category ORDER BY endDateEpochDay ASC, fullName COLLATE NOCASE ASC")
    fun observeByCategory(category: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<MemberEntity?>

    @Query("SELECT * FROM members WHERE endDateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY endDateEpochDay ASC")
    suspend fun getExpiringBetween(startEpochDay: Long, endEpochDay: Long): List<MemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: MemberEntity): Long

    @Update
    suspend fun update(member: MemberEntity)

    @Delete
    suspend fun delete(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :id")
    suspend fun deleteById(id: Long)
}
