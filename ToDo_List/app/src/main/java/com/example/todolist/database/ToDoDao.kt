package com.example.todolist.database

import androidx.room.*
import com.example.todolist.data.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Dao
interface ToDoDao {

    @Transaction
    @Query("SELECT * FROM Note")
    fun getAllNotesFlow(): Flow<List<Note>>

    @Transaction
    @Query("SELECT * FROM Note")
    suspend fun getAllNotes(): List<Note>

    @Query("SELECT * FROM Note WHERE noteID = :noteID")
    suspend fun getNoteByID(noteID: Long): Note

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

}