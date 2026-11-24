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

data class LauncherUiConfig(
    val pinnedPackages: Set<String> = emptySet(),
    val hiddenPackages: Set<String> = emptySet(),
    val favoritePackages: Set<String> = emptySet(),
    val wideCardPackages: Set<String> = emptySet(),
    val customOrder: List<String> = emptyList(),
    val lastOpenedMap: Map<String, Long> = emptyMap(),
    val sortMode: SortMode = SortMode.RECENT,
    val coverStyle: CardCoverStyle = CardCoverStyle.DEFAULT_GRADIENT,
    val customNames: Map<String, String> = emptyMap(),
    val cardWidthDp: Int = 172,
    val cardHeightDp: Int = 104,
    val cardCornerDp: Int = 24,
    val cardSpacingDp: Int = 16,
    val bottomMarginDp: Int = 28,
    val clockTiltDeg: Int = 6,
    val clockScalePct: Int = 100,
    val showDate: Boolean = true,
    val showSecondsInSaver: Boolean = true,
    val showFavoritesRow: Boolean = true,
    val showBannerImages: Boolean = true,
    val showTopStatus: Boolean = true,
    val showQuickChips: Boolean = true,
    val pauseWallpaperOnStop: Boolean = true,
    val wallpaperUri: String = "",
    val wallpaperDimPct: Int = 28,
    val wallpaperBlurDp: Int = 0
)
