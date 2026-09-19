package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
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

        val resolveInfos: List<ResolveInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, 0)
            }
        } catch (_: Exception) {
            emptyList()
        }

        resolveInfos.mapNotNull { resolveInfo ->
            try {
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val packageName = activityInfo.packageName ?: return@mapNotNull null
                val label = try {
                    resolveInfo.loadLabel(pm).toString()
                } catch (_: Exception) {
                    packageName
                }

                val appInfo = activityInfo.applicationInfo

                val iconDrawable = try {
                    resolveInfo.loadIcon(pm)
                } catch (_: Exception) {
                    null
                }

                val iconBitmap = try {
                    iconDrawable?.toBitmap(width = 112, height = 112)?.asImageBitmap()
                } catch (_: Throwable) {
                    null
                }

                val category = determineCategory(label, packageName, appInfo)

                AppItem(
                    label = label,
                    packageName = packageName,
                    icon = iconDrawable,
                    iconBitmap = iconBitmap,
                    category = category
                )
            } catch (_: Exception) {
                null
            }
        }.sortedBy { it.label.lowercase() }
    }

    private fun determineCategory(label: String, packageName: String, appInfo: ApplicationInfo?): String {
        val lowerPkg = packageName.lowercase()
        val lowerLabel = label.lowercase()

        // 1. Social & Communication
        if (lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") || lowerPkg.contains("facebook") ||
            lowerPkg.contains("messenger") || lowerPkg.contains("instagram") || lowerPkg.contains("tiktok") ||
            lowerPkg.contains("snapchat") || lowerPkg.contains("twitter") || lowerPkg.contains("x.com") ||
            lowerPkg.contains("discord") || lowerPkg.contains("signal") || lowerPkg.contains("viber") ||
            lowerPkg.contains("dialer") || lowerPkg.contains("contacts") || lowerPkg.contains("mms") ||
            lowerPkg.contains("sms") || lowerPkg.contains("message") || lowerPkg.contains("telecom") ||
            lowerLabel.contains("message") || lowerLabel.contains("رسائل") || lowerLabel.contains("هاتف") ||
            lowerLabel.contains("جهات الاتصال") || lowerLabel.contains("واتساب") || lowerLabel.contains("تليجرام")
        ) {
            return "social"
        }

        // 2. Media & Entertainment
        if (lowerPkg.contains("youtube") || lowerPkg.contains("music") || lowerPkg.contains("spotify") ||
            lowerPkg.contains("netflix") || lowerPkg.contains("prime") || lowerPkg.contains("video") ||
            lowerPkg.contains("player") || lowerPkg.contains("deezer") || lowerPkg.contains("soundcloud") ||
            lowerPkg.contains("audio") || lowerPkg.contains("podcast") || lowerPkg.contains("tv") ||
            lowerPkg.contains("twitch") || lowerPkg.contains("shazam") || lowerPkg.contains("capcut") ||
            lowerLabel.contains("موسيقى") || lowerLabel.contains("فيديو") || lowerLabel.contains("يوتيوب")
        ) {
            return "media"
        }

        // 3. Productivity & Work
        if (lowerPkg.contains("docs") || lowerPkg.contains("sheets") || lowerPkg.contains("slides") ||
            lowerPkg.contains("drive") || lowerPkg.contains("office") || lowerPkg.contains("word") ||
            lowerPkg.contains("excel") || lowerPkg.contains("mail") || lowerPkg.contains("gmail") ||
            lowerPkg.contains("outlook") || lowerPkg.contains("calendar") || lowerPkg.contains("tasks") ||
            lowerPkg.contains("pdf") || lowerPkg.contains("adobe") || lowerPkg.contains("notion") ||
            lowerPkg.contains("trello") || lowerPkg.contains("slack") || lowerPkg.contains("teams") ||
            lowerPkg.contains("zoom") || lowerPkg.contains("meet") || lowerLabel.contains("بريد") ||
            lowerLabel.contains("تقويم") || lowerLabel.contains("مستندات") || lowerLabel.contains("مهام")
        ) {
            return "productivity"
        }

        // 4. Utilities & System
        if (lowerPkg.contains("camera") || lowerPkg.contains("gallery") || lowerPkg.contains("photos") ||
            lowerPkg.contains("calculator") || lowerPkg.contains("clock") || lowerPkg.contains("alarm") ||
            lowerPkg.contains("settings") || lowerPkg.contains("files") || lowerPkg.contains("filemanager") ||
            lowerPkg.contains("recorder") || lowerPkg.contains("soundrecorder") || lowerPkg.contains("notes") ||
            lowerPkg.contains("keep") || lowerPkg.contains("flashlight") || lowerPkg.contains("torch") ||
            lowerPkg.contains("weather") || lowerPkg.contains("cleaner") || lowerPkg.contains("security") ||
            lowerLabel.contains("إعدادات") || lowerLabel.contains("آلة حاسبة") || lowerLabel.contains("ساعة") ||
            lowerLabel.contains("كاميرا") || lowerLabel.contains("استوديو") || lowerLabel.contains("ملفات") ||
            lowerLabel.contains("ملاحظات") || lowerLabel.contains("طقس")
        ) {
            return "utilities"
        }

        // 5. Navigation & Travel
        if (lowerPkg.contains("maps") || lowerPkg.contains("waze") || lowerPkg.contains("gps") ||
            lowerPkg.contains("navigation") || lowerPkg.contains("uber") || lowerPkg.contains("careem") ||
            lowerPkg.contains("bolt") || lowerPkg.contains("booking") || lowerPkg.contains("airbnb") ||
            lowerLabel.contains("خرائط") || lowerLabel.contains("سفر")
        ) {
            return "navigation"
        }

        // 6. Shopping & Lifestyle
        if (lowerPkg.contains("shop") || lowerPkg.contains("store") || lowerPkg.contains("amazon") ||
            lowerPkg.contains("aliexpress") || lowerPkg.contains("ebay") || lowerPkg.contains("noon") ||
            lowerPkg.contains("jumia") || lowerPkg.contains("shein") || lowerPkg.contains("temu") ||
            lowerPkg.contains("food") || lowerPkg.contains("talabat") || lowerPkg.contains("hungerstation") ||
            lowerPkg.contains("pay") || lowerPkg.contains("wallet") || lowerPkg.contains("bank") ||
            lowerLabel.contains("تسوق") || lowerLabel.contains("شراء") || lowerLabel.contains("بنك")
        ) {
            return "shopping"
        }

        // 7. Information & Browsing
        if (lowerPkg.contains("chrome") || lowerPkg.contains("browser") || lowerPkg.contains("firefox") ||
            lowerPkg.contains("opera") || lowerPkg.contains("edge") || lowerPkg.contains("news") ||
            lowerLabel.contains("متصفح") || lowerLabel.contains("أخبار")
        ) {
            return "browsing"
        }

        // 8. Check Android OS ApplicationInfo categories (API 26+)
        if (appInfo != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (appInfo.category) {
                    ApplicationInfo.CATEGORY_GAME -> return "games"
                    ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_VIDEO -> return "media"
                    ApplicationInfo.CATEGORY_IMAGE, ApplicationInfo.CATEGORY_ACCESSIBILITY -> return "utilities"
                    ApplicationInfo.CATEGORY_SOCIAL -> return "social"
                    ApplicationInfo.CATEGORY_NEWS -> return "browsing"
                    ApplicationInfo.CATEGORY_MAPS -> return "navigation"
                    ApplicationInfo.CATEGORY_PRODUCTIVITY -> return "productivity"
                }
            }
            if ((appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0 || lowerPkg.contains("game") || lowerLabel.contains("لعبة")) {
                return "games"
            }
        }

        return "other"
    }
}

