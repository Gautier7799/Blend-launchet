package com.example.ui.screens.chat

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

// Color constants matching the screenshot exactly
val ChatThemePurple = Color(0xFF6554A7)
val ChatBackgroundLilac = Color(0xFFF3EDF7)
val ChatInputBg = Color(0xFFE9E2EE)
val ChatBubbleBg = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isTyping by viewModel.isTyping.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showClearDialog by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "المساعد الذكي",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "مسح المحادثة",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ChatThemePurple
                    )
                )
            },
            bottomBar = {
                Surface(
                    color = ChatBackgroundLilac,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Purple Circular Send Button
                        FloatingActionButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isTyping) {
                                    val query = inputText
                                    inputText = ""
                                    keyboardController?.hide()
                                    viewModel.sendMessage(query)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(messages.size)
                                    }
                                }
                            },
                            containerColor = ChatThemePurple,
                            contentColor = Color.White,
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(1.dp, 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Input field matching the screenshot
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    text = "اكتب رسالتك هنا...",
                                    color = Color(0xFF49454F),
                                    fontSize = 16.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            },
                            shape = RoundedCornerShape(28.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = ChatInputBg,
                                unfocusedContainerColor = ChatInputBg,
                                disabledContainerColor = ChatInputBg,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                cursorColor = ChatThemePurple,
                                focusedTextColor = Color(0xFF1D1B20),
                                unfocusedTextColor = Color(0xFF1D1B20)
                            ),
                            maxLines = 4
                        )
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatBackgroundLilac)
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        ScreenshotStyledChatBubble(
                            message = message,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(message.text))
                            },
                            onShare = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, message.text)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة الإجابة"))
                            }
                        )
                    }
                }
            }

            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("مسح المحادثة") },
            text = { Text("هل ترغب ببدء محادثة جديدة وتفريغ الرسائل الحالية؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChat()
                        showClearDialog = false
                    }
                ) {
                    Text("مسح", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun ScreenshotStyledChatBubble(
    message: ChatMessage,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    if (message.isUser) {
        // User message on the left (in RTL)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ChatThemePurple,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    } else {
        // Assistant message with Robot Icon on the right (matching screenshot exactly)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            // White bubble
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ChatBubbleBg,
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 270.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                    if (message.isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = ChatThemePurple
                            )
                            Text(
                                text = "جاري كتابة الرد...",
                                color = Color(0xFF49454F),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Text(
                            text = message.text,
                            color = Color(0xFF1D1B20),
                            fontSize = 17.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Normal
                        )

                        if (!message.isError && message.text.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = onCopy,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "نسخ",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF79747E)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = onShare,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "مشاركة",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF79747E)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Robot Avatar Icon exactly as in screenshot
            Surface(
                shape = CircleShape,
                color = Color(0xFF79747E),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "🤖",
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}
