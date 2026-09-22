package com.example.launcher.utils

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.applyBackgroundBlur(radius: Float = 30f): Modifier = this.graphicsLayer {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        renderEffect = RenderEffect
            .createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
            .asComposeRenderEffect()
    }
}
