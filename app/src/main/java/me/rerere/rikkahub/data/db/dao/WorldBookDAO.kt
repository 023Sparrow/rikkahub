package me.rerere.rikkahub.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.rerere.rikkahub.data.db.entity.WorldBookEntry

@Dao
interface WorldBookDAO {
    @Query("SELECT * FROM world_book_entry WHERE assistant_id = :assistantId")
    fun getWorldBookEntriesByAssistant(assistantId: String): Flow<List<WorldBookEntry>>
    
    @Query("SELECT * FROM world_book_entry WHERE assistant_id = :assistantId AND is_enabled = 1")
    suspend fun getActiveWorldBookEntries(assistantId: String): List<WorldBookEntry>
    
    @Query("SELECT * FROM world_book_entry WHERE id = :id")
    suspend fun getWorldBookEntryById(id: String): WorldBookEntry?
    
    @Insert
    suspend fun insert(entry: WorldBookEntry): Long
    
    @Update
    suspend fun update(entry: WorldBookEntry)
    
    @Delete
    suspend fun delete(entry: WorldBookEntry)
    
    @Query("DELETE FROM world_book_entry WHERE assistant_id = :assistantId")
    suspend fun deleteAllForAssistant(assistantId: String)
    
    @Query("SELECT * FROM world_book_entry WHERE assistant_id = :assistantId AND title LIKE :searchText OR keywords LIKE :searchText OR content LIKE :searchText OR comment LIKE :searchText")
    fun searchWorldBookEntries(assistantId: String, searchText: String): Flow<List<WorldBookEntry>>
}