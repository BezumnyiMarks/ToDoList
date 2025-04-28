package com.example.todolist

import com.example.todolist.data.Note
import com.example.todolist.database.AppDatabase
import com.example.todolist.database.ToDoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject


class MainRepository @Inject constructor(
    private val appDatabase: AppDatabase
) {
    fun getAllNotesFlow(): Flow<List<Note>>{
        return appDatabase.notesDao().getAllNotesFlow()
    }

    suspend fun getAllNotes(): List<Note>{
        return appDatabase.notesDao().getAllNotes()
    }

    suspend fun getNoteByID(noteID: Long): Note{
        return appDatabase.notesDao().getNoteByID(noteID)
    }

    suspend fun addNote(note: Note){
        appDatabase.notesDao().insertNote(note)
    }

    suspend fun deleteNote(note: Note){
        appDatabase.notesDao().deleteNote(note)
    }
}