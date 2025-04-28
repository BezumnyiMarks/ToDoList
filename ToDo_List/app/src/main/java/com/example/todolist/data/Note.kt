package com.example.todolist.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Note")
data class Note(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "noteID")
    val noteID: Long ?= null,
    @ColumnInfo(name = "header")
    val header: String = "",
    @ColumnInfo(name = "description")
    val description: String = "",
    @ColumnInfo(name = "done")
    val done: Boolean = false,
    @ColumnInfo(name = "dateTime")
    val dateTime: Long = 0L
)
