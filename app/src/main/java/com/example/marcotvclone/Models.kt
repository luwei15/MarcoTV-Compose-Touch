package com.example.marcotvclone

import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

enum class ActiveScreen {
    DESKTOP,
    ALL_APPS,
    MAGIC_DESKTOP,
    WALLPAPERS,
    SETTINGS,
    SCREENSAVER
}

enum class SortMode(val label: String) {
    RECENT("上次打开优先"),
    ALPHABETICAL("首字母排序"),
    CUSTOM("自定义顺序")
}

enum class CardCoverStyle(val label: String) {
    DEFAULT_GRADIENT("默认渐变"),
    FROSTED_GLASS("通透磨砂"),
    CYAN_VERMILION("青蓝朱红撞色"),
    PURPLE_STACK("紫调叠卡风"),
    DEEP_OBSIDIAN("极夜深黑")
}

data class AppEntry(
    val defaultLabel: String,
    val packageName: String,
    val iconBitmap: ImageBitmap,
    val bannerBitmap: ImageBitmap?,
    val launchIntent: Intent,
    val accentColor: Color
)

data class OfficialWallpaperItem(
    val id: String,
    val title: String,
    val spec: String,
    val desc: String,
    val sha256: String,
    val panUrl: String,
    val previewColorStart: Color,
    val previewColorEnd: Color,
    val defaultStreamUrl: String
)
