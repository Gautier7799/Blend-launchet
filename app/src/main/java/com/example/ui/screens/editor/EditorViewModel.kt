package com.example.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiHelper
import com.example.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditorViewModel(
    private val repository: DocumentRepository,
    private val geminiHelper: GeminiHelper
) : ViewModel() {

    private val _title = MutableStateFlow("مستند بدون عنوان")
    val title = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    private val history = mutableListOf<String>()
    private var historyIndex = -1

    private val _canUndo = MutableStateFlow(false)
    val canUndo = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo = _canRedo.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating = _isAiGenerating.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess = _saveSuccess.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onContentChange(newContent: String) {
        if (_content.value != newContent) {
            pushHistory(_content.value)
            _content.value = newContent
        }
    }

    private fun pushHistory(text: String) {
        if (historyIndex < history.size - 1) {
            history.subList(historyIndex + 1, history.size).clear()
        }
        history.add(text)
        historyIndex = history.size - 1
        updateHistoryStates()
    }

    fun undo() {
        if (historyIndex >= 0) {
            val previous = history[historyIndex]
            historyIndex--
            _content.value = previous
            updateHistoryStates()
        }
    }

    fun redo() {
        if (historyIndex + 1 < history.size) {
            historyIndex++
            val next = history[historyIndex]
            _content.value = next
            updateHistoryStates()
        }
    }

    private fun updateHistoryStates() {
        _canUndo.value = historyIndex >= 0
        _canRedo.value = historyIndex + 1 < history.size
    }

    fun appendFormatting(prefix: String, suffix: String = "") {
        pushHistory(_content.value)
        _content.value = "${_content.value}\n$prefix$suffix"
    }

    fun aiAssist(instruction: String) {
        val currentText = _content.value
        _isAiGenerating.value = true
        viewModelScope.launch {
            val prompt = "أنت محرر نصوص ذكي في Google Docs. بناءً على هذا النص:\n'$currentText'\nالطلب: $instruction\nقم بكتابة النص المُحسّن أو المُضاف باللغة العربية بأسلوب راقٍ واحترافي."
            val response = geminiHelper.summarize(prompt)
            pushHistory(_content.value)
            _content.value = if (currentText.isBlank()) response else "$currentText\n\n$response"
            _isAiGenerating.value = false
        }
    }

    fun saveDocument() {
        if (_title.value.isBlank() || _content.value.isBlank()) return

        _isSaving.value = true
        viewModelScope.launch {
            val result = repository.createTextDocument(_title.value, _content.value)
            _isSaving.value = false
            if (result.isSuccess) {
                _saveSuccess.value = true
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: DocumentRepository,
            geminiHelper: GeminiHelper
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EditorViewModel(repository, geminiHelper) as T
                }
            }
    }
}
