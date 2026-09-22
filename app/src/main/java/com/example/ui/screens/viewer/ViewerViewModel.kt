package com.example.launcher.ui.screens.viewer

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.launcher.data.model.Document
import com.example.launcher.data.model.Note
import com.example.launcher.repository.DocumentRepository

class ViewerViewModel(
    private val repository: DocumentRepository = DocumentRepository()
) : ViewModel() {

    fun loadDocument(id: String): Document? {
        return repository.getDocuments().find { it.id == id }
    }

    fun filterNotesByText(query: String): List<Note> {
        return repository.filterNotes { note ->
            note.text.contains(query, ignoreCase = true)
        }
    }

    fun processContent(context: Context, id: String) {
        // تنفيذ العمليات المرتبطة بـ Context و ID
    }
}
