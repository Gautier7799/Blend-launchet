package com.example.domain

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap

data class AppItem(
    val label: String,
    val packageName: String,
    val icon: Drawable? = null,
    val iconBitmap: ImageBitmap? = null
)
