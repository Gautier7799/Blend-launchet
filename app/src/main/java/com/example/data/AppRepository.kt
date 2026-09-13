package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.example.domain.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    suspend fun getInstalledApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }

        resolveInfos.mapNotNull { resolveInfo ->
            try {
                val label = resolveInfo.loadLabel(pm).toString()
                val packageName = resolveInfo.activityInfo.packageName
                val iconDrawable = resolveInfo.loadIcon(pm)
                val iconBitmap = try {
                    iconDrawable.toBitmap(width = 128, height = 128).asImageBitmap()
                } catch (_: Throwable) {
                    null
                }

                AppItem(
                    label = label,
                    packageName = packageName,
                    icon = iconDrawable,
                    iconBitmap = iconBitmap
                )
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.label.lowercase() }
    }
}

