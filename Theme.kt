package com.securevault.data.db

import androidx.room.*
import com.securevault.data.model.VaultEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_entries ORDER BY updatedAt DESC")
    fun getAllEntries(): Flow<List<VaultEntry>>

    @Query("SELECT * FROM vault_entries WHERE title LIKE '%' || :q || '%' ORDER BY updatedAt DESC")
    fun searchEntries(q: String): Flow<List<VaultEntry>>

    @Query("SELECT * FROM vault_entries WHERE id = :id")
    suspend fun getById(id: Long): VaultEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: VaultEntry): Long

    @Update
    suspend fun update(entry: VaultEntry)

    @Query("DELETE FROM vault_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT DISTINCT category FROM vault_entries ORDER BY category")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM vault_entries")
    fun getCount(): Flow<Int>
}
