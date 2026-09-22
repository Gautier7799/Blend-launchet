package com.example.launcher.data.model

data class Document(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Note(
    val id: String = "",
    val text: String = ""
)
