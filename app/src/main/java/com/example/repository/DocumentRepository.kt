package com.example.launcher.repository

import com.example.launcher.data.model.Document
import com.example.launcher.data.model.Note

class DocumentRepository {
    private val documents = mutableListOf<Document>()
    private val notes = mutableListOf<Note>()

    fun getDocuments(): List<Document> = documents

    fun addDocument(doc: Document) {
        documents.add(doc)
    }

    fun getNotes(): List<Note> = notes

    fun filterNotes(predicate: (Note) -> Boolean): List<Note> {
        return notes.filter(predicate)
    }
}
