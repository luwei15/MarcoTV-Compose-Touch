package com.example.marcotvclone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: MarcoViewModel = viewModel()
            LaunchedEffect(Unit) {
                vm.loadState(this@MainActivity)
                vm.loadInstalledApps(this@MainActivity)
            }
            MarcoTouchRoot(vm)
        }
    }
}

@Composable
fun MarcoTouchRoot(vm: MarcoViewModel) {
    val context = LocalContext.current
    val activeScreen by vm.activeScreen.collectAsState()
    val wallpaperUri by vm.wallpaperUri.collectAsState()
    val dimPct by vm.wallpaperDimPct.collectAsState()
    val blurDp by vm.wallpaperBlurDp.collectAsState()

    val allApps by vm.allApps.collectAsState()
    val pinnedPkgs by vm.pinnedPackages.collectAsState()
    val hiddenPkgs by vm.hiddenPackages.collectAsState()
    val favoritePkgs by vm.favoritePackages.collectAsState()
    val widePkgs by vm.wideCardPackages.collectAsState()
    val customOrder by vm.customOrder.collectAsState()
    val lastOpenedMap by vm.lastOpenedMap.collectAsState()
    val sortMode by vm.sortMode.collectAsState()
    val coverStyle by vm.cardCoverStyle.collectAsState()
    val customNames by vm.customNames.collectAsState()

    val cardWidth by vm.cardWidthDp.collectAsState()
    val cardHeight by vm.cardHeightDp.collectAsState()
    val cardCorner by vm.cardCornerDp.collectAsState()
    val cardSpacing by vm.cardSpacingDp.collectAsState()
    val bottomMargin by vm.bottomMarginDp.collectAsState()
    val clockTilt by vm.clockTiltDeg.collectAsState()
    val clockScale by vm.clockScalePct.collectAsState()
    val showDate by vm.showDate.collectAsState()
    val showSecondsInSaver by vm.showSecondsInSaver.collectAsState()
    val showFavoritesRow by vm.showFavoritesRow.collectAsState()
    val showBannerImages by vm.showBannerImages.collectAsState()
    val showTopStatus by vm.showTopStatus.collectAsState()
    val showQuickChips by vm.showQuickChips.collectAsState()
    val pauseWallpaperOnStop by vm.pauseWallpaperOnStop.collectAsState()

    val mirrorStatus by vm.onlineMirrorStatus.collectAsState()
    val parsedVideos by vm.parsedDirectVideos.collectAsState()
    val shaVerifyResult by vm.shaVerifyResult.collectAsState()
    val cacheProgressText by vm.cacheProgressText.collectAsState()

    var editingApp by remember { mutableStateOf<AppEntry?>(null) }

    val sortedVisibleApps = remember(allApps, pinnedPkgs, hiddenPkgs, sortMode, lastOpenedMap, customOrder, customNames) {
        val visible = allApps.filterNot { hiddenPkgs.contains(it.packageName) }
        val comparator: Comparator<AppEntry> = when (sortMode) {
            SortMode.RECENT -> compareByDescending<AppEntry> { lastOpenedMap[it.packageName] ?: 0L }
                .thenBy { (customNames[it.packageName] ?: it.defaultLabel).lowercase() }
            SortMode.ALPHABETICAL -> compareBy { (customNames[it.packageName] ?: it.defaultLabel).lowercase() }
            SortMode.CUSTOM -> compareBy<AppEntry> {
                val idx = customOrder.indexOf(it.packageName)
                if (idx >= 0) idx else Int.MAX_VALUE
            }.thenBy { (customNames[it.packageName] ?: it.defaultLabel).lowercase() }
        }
        val pinned = visible.filter { pinnedPkgs.contains(it.packageName) }.sortedWith(comparator)
        val normal = visible.filterNot { pinnedPkgs.contains(it.packageName) }.sortedWith(comparator)
        pinned + normal
    }

    val favoriteApps = remember(sortedVisibleApps, favoritePkgs) {
        sortedVisibleApps.filter { favoritePkgs.contains(it.packageName) }
    }

    val launchApp: (AppEntry) -> Unit = { app ->
        try {
            vm.recordAppLaunched(context, app.packageName)
            context.startActivity(app.launchIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "无法启动 ${vm.displayLabel(app)}", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0D14))) {
        ReusableLoopingVideoBackdrop(
            uriString = wallpaperUri,
            blurDp = blurDp,
            pauseOnStop = pauseWallpaperOnStop
        )

        val topAlpha = (dimPct / 100f * 0.85f).coerceIn(0.08f, 0.75f)
        val bottomAlpha = (dimPct / 100f * 1.35f + 0.18f).coerceIn(0.22f, 0.88f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF070A12).copy(alpha = topAlpha),
                            Color(0xFF101322).copy(alpha = (dimPct / 100f * 0.45f).coerceIn(0.04f, 0.45f)),
                            Color(0xFF06080F).copy(alpha = bottomAlpha)
                        )
                    )
                )
        )

        if (activeScreen == ActiveScreen.SCREENSAVER) {
            ScreensaverClockOverlay(
                tiltDeg = clockTilt,
                showSeconds = showSecondsInSaver,
                onDismiss = { vm.navigateTo(ActiveScreen.DESKTOP) }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 36.dp,
                        end = 36.dp,
                        top = 24.dp,
                        bottom = bottomMargin.dp
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                MarcoTopBar(
                    activeScreen = activeScreen,
                    clockTiltDeg = clockTilt,
                    clockScalePct = clockScale,
                    showDate = showDate,
                    showTopStatus = showTopStatus,
                    sortMode = sortMode,
                    onSelectScreen = { vm.navigateTo(it) },
                    onCycleSortMode = {
                        val next = when (sortMode) {
                            SortMode.RECENT -> SortMode.ALPHABETICAL
                            SortMode.ALPHABETICAL -> SortMode.CUSTOM
                            SortMode.CUSTOM -> SortMode.RECENT
                        }
                        vm.setSortMode(context, next)
                    },
                    onEnterScreensaver = { vm.navigateTo(ActiveScreen.SCREENSAVER) }
                )

                AnimatedContent(
                    targetState = activeScreen,
                    transitionSpec = {
                        (fadeIn(tween(240, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.97f, animationSpec = tween(260, easing = FastOutSlowInEasing)))
                            .togetherWith(
                                fadeOut(tween(180)) +
                                    scaleOut(targetScale = 0.985f, animationSpec = tween(180))
                            )
                    },
                    label = "screenTransition",
                    modifier = Modifier.weight(1f)
                ) { screen ->
                    when (screen) {
                        ActiveScreen.DESKTOP -> {
                            MarcoDesktopHome(
                                apps = sortedVisibleApps,
                                favoriteApps = favoriteApps,
                                showFavoritesRow = showFavoritesRow,
                                showBannerImages = showBannerImages,
                                showQuickChips = showQuickChips,
                                widePkgs = widePkgs,
                                pinnedPkgs = pinnedPkgs,
                                customNames = customNames,
                                coverStyle = coverStyle,
                                cardWidth = cardWidth.dp,
                                cardHeight = cardHeight.dp,
                                cardCorner = cardCorner.dp,
                                cardSpacing = cardSpacing.dp,
                                onLaunchApp = launchApp,
                                onLongPressCard = { editingApp = it },
                                onOpenAllApps = { vm.navigateTo(ActiveScreen.ALL_APPS) },
                                onOpenWallpapers = { vm.navigateTo(ActiveScreen.WALLPAPERS) }
                            )
                        }
                        ActiveScreen.ALL_APPS -> {
                            AllAppsDrawerScreen(
                                allApps = allApps,
                                pinnedPkgs = pinnedPkgs,
                                hiddenPkgs = hiddenPkgs,
                                favoritePkgs = favoritePkgs,
                                customNames = customNames,
                                onLaunchApp = launchApp,
                                onEditApp = { editingApp = it },
                                onBack = { vm.navigateTo(ActiveScreen.DESKTOP) }
                            )
                        }
                        ActiveScreen.MAGIC_DESKTOP -> {
                            MagicDesktopStudioScreen(
                                cardWidth = cardWidth,
                                cardHeight = cardHeight,
                                cardCorner = cardCorner,
                                cardSpacing = cardSpacing,
                                bottomMargin = bottomMargin,
                                clockTilt = clockTilt,
                                clockScale = clockScale,
                                dimPct = dimPct,
                                blurDp = blurDp,
                                showFavorites = showFavoritesRow,
                                showDate = showDate,
                                showSecondsInSaver = showSecondsInSaver,
                                showBannerImages = showBannerImages,
                                showTopStatus = showTopStatus,
                                showQuickChips = showQuickChips,
                                pauseWallpaperOnStop = pauseWallpaperOnStop,
                                coverStyle = coverStyle,
                                sampleApp = sortedVisibleApps.firstOrNull(),
                                onChangeCoverStyle = { vm.setCardCoverStyle(context, it) },
                                onApplyTouchPreset = {
                                    vm.applyTouchConsolePreset(context)
                                    Toast.makeText(context, "已套用大屏触控精简预设", Toast.LENGTH_SHORT).show()
                                },
                                onUpdate = { w, h, c, sp, b, t, s, d, bl, fav, dt, sec, ban, st, chips, pauseFlag ->
                                    vm.updateMagicAppearance(context, w, h, c, sp, b, t, s, d, bl, fav, dt, sec, ban, st, chips, pauseFlag)
                                },
                                onReset = { vm.resetMagicAppearance(context) },
                                onBack = { vm.navigateTo(ActiveScreen.DESKTOP) }
                            )
                        }
                        ActiveScreen.WALLPAPERS -> {
                            WallpapersCenterScreen(
                                currentUri = wallpaperUri,
                                officialWallpapers = vm.officialWallpapers,
                                mirrorStatus = mirrorStatus,
                                parsedVideos = parsedVideos,
                                shaVerifyResult = shaVerifyResult,
                                cacheProgressText = cacheProgressText,
                                onSyncMirror = { vm.syncWallpaperMirror() },
                                onCacheCurrentLocally = { vm.cacheCurrentWallpaperLocally(context) },
                                onVerifyLocalUri = { vm.verifyLocalVideoSha256(context, it) },
                                onSelectUri = { vm.applyWallpaper(context, it) },
                                onBack = { vm.navigateTo(ActiveScreen.DESKTOP) }
                            )
                        }
                        ActiveScreen.SETTINGS -> {
                            DualPaneSettingsScreen(
                                allApps = allApps,
                                hiddenPkgs = hiddenPkgs,
                                pinnedPkgs = pinnedPkgs,
                                sortMode = sortMode,
                                onSelectSortMode = { vm.setSortMode(context, it) },
                                onToggleHide = { vm.toggleHidden(context, it) },
                                onExportBackup = { vm.exportBackupJson(context) },
                                onImportBackup = { vm.importBackupJson(context, it) },
                                onOpenScreensaver = { vm.navigateTo(ActiveScreen.SCREENSAVER) },
                                onBack = { vm.navigateTo(ActiveScreen.DESKTOP) }
                            )
                        }
                        ActiveScreen.SCREENSAVER -> Unit
                    }
                }
            }
        }

        editingApp?.let { target ->
            val orderedPkgs = sortedVisibleApps.map { it.packageName }
            SingleCardManageDialog(
                app = target,
                currentLabel = vm.displayLabel(target),
                isPinned = pinnedPkgs.contains(target.packageName),
                isFavorite = favoritePkgs.contains(target.packageName),
                isHidden = hiddenPkgs.contains(target.packageName),
                isWide = widePkgs.contains(target.packageName),
                onRename = { vm.renameApp(context, target.packageName, it) },
                onTogglePin = { vm.togglePinned(context, target.packageName) },
                onToggleFavorite = { vm.toggleFavorite(context, target.packageName) },
                onToggleWide = { vm.toggleWideCard(context, target.packageName) },
                onToggleHide = { vm.toggleHidden(context, target.packageName) },
                onMoveLeft = { vm.moveAppInCustomOrder(context, target.packageName, -1, orderedPkgs) },
                onMoveRight = { vm.moveAppInCustomOrder(context, target.packageName, 1, orderedPkgs) },
                onUninstall = {
                    val uninstallIntent = Intent(Intent.ACTION_DELETE, Uri.parse("package:${target.packageName}"))
                    try {
                        context.startActivity(uninstallIntent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "系统限制无法卸载此应用", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = { editingApp = null }
            )
        }
    }
}
