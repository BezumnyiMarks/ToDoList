package com.example.todolist.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todolist.data.Note


@Database(entities = [Note::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract  fun notesDao(): ToDoDao
}