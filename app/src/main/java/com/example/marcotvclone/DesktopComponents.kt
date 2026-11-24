package com.example.marcotvclone

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.view.HapticFeedbackConstants
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun ReusableLoopingVideoBackdrop(
    uriString: String,
    blurDp: Int,
    pauseOnStop: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            playWhenReady = true
        }
    }

    LaunchedEffect(uriString) {
        try {
            player.setMediaItem(MediaItem.fromUri(Uri.parse(uriString)))
            player.prepare()
            player.playWhenReady = true
        } catch (_: Exception) {
        }
    }

    DisposableEffect(lifecycleOwner, pauseOnStop) {
        val observer = LifecycleEventObserver { _, event ->
            if (!pauseOnStop) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_STOP -> player.pause()
                Lifecycle.Event.ON_START -> player.play()
                else -> Unit
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
            player.release()
        }
    }

    val modifier = if (blurDp > 0) {
        Modifier.fillMaxSize().blur(blurDp.dp)
    } else {
        Modifier.fillMaxSize()
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = modifier
    )
}

@Composable
fun MarcoTopBar(
    activeScreen: ActiveScreen,
    clockTiltDeg: Int,
    clockScalePct: Int,
    showDate: Boolean,
    showTopStatus: Boolean,
    sortMode: SortMode,
    onSelectScreen: (ActiveScreen) -> Unit,
    onCycleSortMode: () -> Unit,
    onEnterScreensaver: () -> Unit
) {
    val context = LocalContext.current
    var timeText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var wifiLabel by remember { mutableStateOf("WLAN") }
    var batteryPct by remember { mutableStateOf(-1) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            dateText = SimpleDateFormat("M月d日 E", Locale.CHINA).format(now)
            wifiLabel = try {
                val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val ssid = wm?.connectionInfo?.ssid?.replace("\"", "") ?: ""
                if (ssid.isNotBlank() && ssid != "<unknown ssid>") ssid else "已连接"
            } catch (_: Exception) {
                "WLAN"
            }
            batteryPct = try {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                val lvl = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
                if (lvl in 1..100) lvl else -1
            } catch (_: Exception) {
                -1
            }
            delay(2000)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopCapsuleButton(
                text = "⚙️ 设置",
                selected = activeScreen == ActiveScreen.SETTINGS,
                onClick = { onSelectScreen(ActiveScreen.SETTINGS) }
            )
            TopCapsuleButton(
                text = "MARCO TV",
                selected = activeScreen == ActiveScreen.DESKTOP,
                isBrand = true,
                onClick = { onSelectScreen(ActiveScreen.DESKTOP) }
            )
            TopCapsuleButton(
                text = "Magic Desktop",
                selected = activeScreen == ActiveScreen.MAGIC_DESKTOP,
                onClick = { onSelectScreen(ActiveScreen.MAGIC_DESKTOP) }
            )
            TopCapsuleButton(
                text = "我的壁纸",
                selected = activeScreen == ActiveScreen.WALLPAPERS,
                onClick = { onSelectScreen(ActiveScreen.WALLPAPERS) }
            )
            TopCapsuleButton(
                text = "全部应用",
                selected = activeScreen == ActiveScreen.ALL_APPS,
                onClick = { onSelectScreen(ActiveScreen.ALL_APPS) }
            )
            if (activeScreen == ActiveScreen.DESKTOP) {
                TopCapsuleButton(
                    text = "排序：${sortMode.label}",
                    selected = false,
                    onClick = onCycleSortMode
                )
            }
        }

        // 右侧玻璃状态胶囊：时钟、日期与 Wi-Fi 横向错落排布，增加柔和玻璃底托提升可读性
        val scaleFactor = clockScalePct / 100f
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Black.copy(alpha = 0.24f), Color.Black.copy(alpha = 0.42f))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.06f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onEnterScreensaver() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            if (showTopStatus) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable {
                            try {
                                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                            } catch (_: Exception) {
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    val batStr = if (batteryPct > 0) " · 🔋$batteryPct%" else ""
                    Text(
                        text = "📶 $wifiLabel$batStr",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            if (showDate) {
                Text(
                    text = dateText,
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Text(
                text = timeText,
                color = Color.White,
                fontSize = (36 * scaleFactor).sp,
                fontWeight = FontWeight.Bold,
                fontStyle = if (clockTiltDeg > 0) FontStyle.Italic else FontStyle.Normal,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.rotate(-clockTiltDeg * 0.35f)
            )
        }
    }
}

@Composable
fun TopCapsuleButton(
    text: String,
    selected: Boolean,
    isBrand: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "navScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    selected -> Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.32f), Color.White.copy(alpha = 0.14f))
                    )
                    isBrand -> Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.07f))
                    )
                    else -> Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.04f))
                    )
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        if (selected) Color.White.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.06f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 15.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = if (selected || isBrand) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun MarcoDesktopHome(
    apps: List<AppEntry>,
    favoriteApps: List<AppEntry>,
    showFavoritesRow: Boolean,
    showBannerImages: Boolean,
    showQuickChips: Boolean,
    widePkgs: Set<String>,
    pinnedPkgs: Set<String>,
    customNames: Map<String, String>,
    coverStyle: CardCoverStyle,
    cardWidth: Dp,
    cardHeight: Dp,
    cardCorner: Dp,
    cardSpacing: Dp,
    onLaunchApp: (AppEntry) -> Unit,
    onLongPressCard: (AppEntry) -> Unit,
    onOpenAllApps: () -> Unit,
    onOpenWallpapers: () -> Unit
) {
    val favRowState = rememberLazyListState()
    val mainRowState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        if (showQuickChips) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickGhostChip(label = "4K 动态壁纸与直链", onClick = onOpenWallpapers)
                    QuickGhostChip(label = "五列应用抽屉 (${apps.size})", onClick = onOpenAllApps)
                }
                Text(
                    text = "左右滑动浏览 · 点按启动 · 长按卡片管理",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        AnimatedVisibility(
            visible = showFavoritesRow && favoriteApps.isNotEmpty(),
            enter = fadeIn(tween(240, easing = FastOutSlowInEasing)) + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(260, easing = FastOutSlowInEasing)
            ),
            exit = fadeOut(tween(180)) + slideOutVertically(
                targetOffsetY = { it / 5 },
                animationSpec = tween(180)
            )
        ) {
            Column(modifier = Modifier.padding(bottom = 22.dp)) {
                Text(
                    text = "收藏",
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )
                MotionCardRow(
                    rowState = favRowState,
                    apps = favoriteApps,
                    keyPrefix = "fav",
                    widePkgs = widePkgs,
                    pinnedPkgs = pinnedPkgs,
                    customNames = customNames,
                    coverStyle = coverStyle,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    cardCorner = cardCorner,
                    cardSpacing = cardSpacing,
                    showBanner = showBannerImages,
                    onLaunchApp = onLaunchApp,
                    onLongPressCard = onLongPressCard
                )
            }
        }

        Column {
            Text(
                text = "我的应用",
                color = Color.White.copy(alpha = 0.94f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )
            MotionCardRow(
                rowState = mainRowState,
                apps = apps,
                keyPrefix = "main",
                widePkgs = widePkgs,
                pinnedPkgs = pinnedPkgs,
                customNames = customNames,
                coverStyle = coverStyle,
                cardWidth = cardWidth,
                cardHeight = cardHeight,
                cardCorner = cardCorner,
                cardSpacing = cardSpacing,
                showBanner = showBannerImages,
                onLaunchApp = onLaunchApp
            )
        }
    }
}

@Composable
private fun MotionCardRow(
    rowState: LazyListState,
    apps: List<AppEntry>,
    keyPrefix: String,
    widePkgs: Set<String>,
    pinnedPkgs: Set<String>,
    customNames: Map<String, String>,
    coverStyle: CardCoverStyle,
    cardWidth: Dp,
    cardHeight: Dp,
    cardCorner: Dp,
    cardSpacing: Dp,
    showBanner: Boolean,
    onLaunchApp: (AppEntry) -> Unit,
    onLongPressCard: (AppEntry) -> Unit
) {
    val isScrolling by remember {
        derivedStateOf { rowState.isScrollInProgress }
    }
    val velocityFactor by animateFloatAsState(
        targetValue = if (isScrolling) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "rowScrollScale"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            state = rowState,
            horizontalArrangement = Arrangement.spacedBy(cardSpacing),
            contentPadding = PaddingValues(start = 4.dp, end = 48.dp)
        ) {
            items(apps, key = { "${keyPrefix}_${it.packageName}" }) { app ->
                val isWide = widePkgs.contains(app.packageName)
                val label = customNames[app.packageName]?.takeIf { it.isNotBlank() } ?: app.defaultLabel
                MarcoHorizontalCard(
                    app = app,
                    displayLabel = label,
                    width = if (isWide) cardWidth * 1.38f else cardWidth,
                    height = cardHeight,
                    cornerRadius = cardCorner,
                    isPinned = pinnedPkgs.contains(app.packageName),
                    isWide = isWide,
                    showBanner = showBanner,
                    coverStyle = coverStyle,
                    scrollScale = velocityFactor,
                    onClick = { onLaunchApp(app) },
                    onLongPress = { onLongPressCard(app) }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(56.dp)
                .height(cardHeight + 28.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF070A12).copy(alpha = 0.55f))
                    )
                )
        )
    }
}

@Composable
fun QuickGhostChip(label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.14f), Color.Black.copy(alpha = 0.35f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.32f), Color.White.copy(alpha = 0.08f))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 13.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun MarcoHorizontalCard(
    app: AppEntry,
    displayLabel: String,
    width: Dp,
    height: Dp,
    cornerRadius: Dp,
    isPinned: Boolean,
    isWide: Boolean,
    showBanner: Boolean,
    coverStyle: CardCoverStyle,
    scrollScale: Float = 1f,
    onClick: () -> Unit,
    onLongPress: (() -> Unit)? = null
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cardPress"
    )
    val tiltY by animateFloatAsState(
        targetValue = if (isPressed) -2.5f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardTilt"
    )

    val cardBrush = when (coverStyle) {
        CardCoverStyle.DEFAULT_GRADIENT -> Brush.linearGradient(
            listOf(app.accentColor.copy(alpha = 0.72f), Color(0xFF131826).copy(alpha = 0.88f))
        )
        CardCoverStyle.FROSTED_GLASS -> Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.24f), Color.White.copy(alpha = 0.08f))
        )
        CardCoverStyle.CYAN_VERMILION -> Brush.linearGradient(
            listOf(Color(0xFF0284C7).copy(alpha = 0.76f), Color(0xFFDC2626).copy(alpha = 0.76f))
        )
        CardCoverStyle.PURPLE_STACK -> Brush.linearGradient(
            listOf(Color(0xFF8B5CF6).copy(alpha = 0.85f), Color(0xFF311B92).copy(alpha = 0.90f))
        )
        CardCoverStyle.DEEP_OBSIDIAN -> Brush.linearGradient(
            listOf(Color(0xFF1A2030).copy(alpha = 0.92f), Color(0xFF090C12).copy(alpha = 0.95f))
        )
    }

    val rimBrush = Brush.verticalGradient(
        colors = listOf(
            if (isPinned) Color(0xFFD8B4FE) else Color.White.copy(alpha = 0.36f),
            Color.White.copy(alpha = 0.06f)
        )
    )

    Column(
        modifier = Modifier
            .width(width)
            .graphicsLayer {
                val combined = pressScale * scrollScale
                scaleX = combined
                scaleY = combined
                rotationY = tiltY
                cameraDistance = 12f * density
            }
            .pointerInput(app.packageName) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() },
                    onLongPress = {
                        if (onLongPress != null) {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onLongPress()
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(cornerRadius))
                .background(cardBrush)
                .border(
                    width = 1.dp,
                    brush = rimBrush,
                    shape = RoundedCornerShape(cornerRadius)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height * 0.42f)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.14f), Color.Transparent)
                        )
                    )
            )

            // 统一视觉语言：宽卡与标准卡都以干净图标/横幅为核心，不塞入零碎说明文字
            if (showBanner && app.bannerBitmap != null) {
                Image(
                    bitmap = app.bannerBitmap,
                    contentDescription = displayLabel,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val iconSize = if (isWide) 66.dp else 56.dp
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = app.iconBitmap,
                        contentDescription = displayLabel,
                        modifier = Modifier
                            .size(iconSize)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            if (isPinned) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF7C4DFF).copy(alpha = 0.88f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "置顶",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = displayLabel,
            color = Color.White.copy(alpha = if (isPressed) 1f else 0.94f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SingleCardManageDialog(
    app: AppEntry,
    currentLabel: String,
    isPinned: Boolean,
    isFavorite: Boolean,
    isHidden: Boolean,
    isWide: Boolean,
    onRename: (String) -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleWide: () -> Unit,
    onToggleHide: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit
) {
    var nameInput by remember(currentLabel) { mutableStateOf(currentLabel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF171C2B),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text("卡片管理 · ${app.defaultLabel}", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("自定义显示名称（最多 32 字）", maxLines = 1, softWrap = false) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFB388FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onTogglePin, modifier = Modifier.weight(1f)) {
                        Text(if (isPinned) "取消置顶" else "置顶优先", maxLines = 1, softWrap = false)
                    }
                    Button(onClick = onToggleFavorite, modifier = Modifier.weight(1f)) {
                        Text(if (isFavorite) "移出收藏" else "加入收藏", maxLines = 1, softWrap = false)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onMoveLeft, modifier = Modifier.weight(1f)) {
                        Text("← 左移一位", color = Color.White, maxLines = 1, softWrap = false)
                    }
                    OutlinedButton(onClick = onMoveRight, modifier = Modifier.weight(1f)) {
                        Text("右移一位 →", color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onToggleWide, modifier = Modifier.weight(1f)) {
                        Text(if (isWide) "切为标准卡" else "切为 16:9 宽卡", color = Color.White, maxLines = 1, softWrap = false)
                    }
                    OutlinedButton(onClick = onToggleHide, modifier = Modifier.weight(1f)) {
                        Text(if (isHidden) "取消隐藏" else "从桌面隐藏", color = Color(0xFFFF8A80), maxLines = 1, softWrap = false)
                    }
                }
                TextButton(onClick = {
                    onDismiss()
                    onUninstall()
                }) {
                    Text("卸载此应用（系统确认）", color = Color(0xFFFF5252), maxLines = 1, softWrap = false)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onRename(nameInput)
                onDismiss()
            }) {
                Text("保存并关闭", maxLines = 1, softWrap = false)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.White.copy(alpha = 0.8f), maxLines = 1, softWrap = false)
            }
        }
    )
}
