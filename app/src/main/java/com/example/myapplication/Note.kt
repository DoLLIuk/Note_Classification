package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String?,
    var content: String?,
    var category: String? = null
)