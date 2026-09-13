package com.example.model

data class Document(
    val id: String = "",
    val title: String = "",
    val type: String = "txt", // "txt" or "pdf"
    val uploadDate: Long = System.currentTimeMillis(),
    val size: Long = 0L,
    val storageUrl: String = "",
    val tags: List<String> = emptyList(),
    val content: String = "" // For txt documents, store directly in firestore or storage. If small, firestore.
)

data class Note(
    val id: String = "",
    val documentId: String = "",
    val content: String = "",
    val pageOrChapter: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
