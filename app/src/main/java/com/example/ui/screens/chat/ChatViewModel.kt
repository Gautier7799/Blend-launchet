package com.example.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

class ChatViewModel(private val geminiHelper: GeminiHelper) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "مرحباً بك! أنا المساعد الذكي. كيف يمكنني مساعدتك اليوم؟",
                isUser = false
            )
        )
    )
    val messages = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping = _isTyping.asStateFlow()

    val quickPrompts = listOf(
        "📄 كيف أصيغ خطاب عمل رسمي؟",
        "💡 اقترح خطة لتنظيم المستندات",
        "✍️ اكتب مقدمة احترافية لتقرير",
        "🧠 ما هي أحدث نماذج الذكاء الاصطناعي؟"
    )

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, isUser = true)
        val loadingMessage = ChatMessage(text = "", isUser = false, isLoading = true)

        val currentList = _messages.value
        _messages.value = currentList + userMessage + loadingMessage
        _isTyping.value = true

        viewModelScope.launch {
            val history = currentList
                .filter { !it.isLoading && !it.isError }
                .takeLast(10)
                .map { Pair(it.text, it.isUser) }

            val reply = geminiHelper.chat(history, text)

            _messages.value = _messages.value.map {
                if (it.isLoading) {
                    ChatMessage(
                        text = reply,
                        isUser = false,
                        isError = reply.startsWith("عذراً، حدث خطأ") || reply.startsWith("مفتاح Gemini API")
                    )
                } else it
            }
            _isTyping.value = false
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                text = "تم بدء محادثة جديدة! تفضل بطرح أي سؤال أو طلب كتابة مستند وسأساعدك فوراً.",
                isUser = false
            )
        )
    }

    companion object {
        fun provideFactory(geminiHelper: GeminiHelper): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatViewModel(geminiHelper) as T
                }
            }
    }
}
