package com.example.marcotvclone

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AllAppsDrawerScreen(
    allApps: List<AppEntry>,
    pinnedPkgs: Set<String>,
    hiddenPkgs: Set<String>,
    favoritePkgs: Set<String>,
    customNames: Map<String, String>,
    onLaunchApp: (AppEntry) -> Unit,
    onEditApp: (AppEntry) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(allApps, searchQuery, customNames) {
        if (searchQuery.isBlank()) allApps
        else {
            val q = searchQuery.trim().lowercase(Locale.getDefault())
            allApps.filter { app ->
                val label = (customNames[app.packageName] ?: app.defaultLabel).lowercase(Locale.getDefault())
                label.contains(q) || app.packageName.lowercase(Locale.getDefault()).contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xCC0E121D))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "全部应用抽屉（${filtered.size} / ${allApps.size}）",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索应用名称或包名") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFB388FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                        cursorColor = Color(0xFFB388FF)
                    ),
                    modifier = Modifier.width(280.dp)
                )
                Button(onClick = onBack) { Text("返回桌面") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.packageName }) { app ->
                val label = customNames[app.packageName]?.takeIf { it.isNotBlank() } ?: app.defaultLabel
                val isHidden = hiddenPkgs.contains(app.packageName)
                val isPinned = pinnedPkgs.contains(app.packageName)
                val isFav = favoritePkgs.contains(app.packageName)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isHidden) Color(0xFF7F1D1D).copy(alpha = 0.35f)
                            else Color.White.copy(alpha = 0.08f)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                        .clickable { onLaunchApp(app) }
                        .padding(14.dp)
                ) {
                    Image(
                        bitmap = app.iconBitmap,
                        contentDescription = label,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val tagText = buildList {
                        if (isPinned) add("置顶")
                        if (isFav) add("收藏")
                        if (isHidden) add("已隐藏")
                    }.joinToString(" · ").ifEmpty { "点击管理" }
                    TextButton(onClick = { onEditApp(app) }) {
                        Text(
                            text = tagText,
                            color = if (isHidden) Color(0xFFFF8A80) else Color(0xFFB388FF),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MagicDesktopStudioScreen(
    cardWidth: Int,
    cardHeight: Int,
    cardCorner: Int,
    cardSpacing: Int,
    bottomMargin: Int,
    clockTilt: Int,
    clockScale: Int,
    dimPct: Int,
    blurDp: Int,
    showFavorites: Boolean,
    showDate: Boolean,
    showSecondsInSaver: Boolean,
    showBannerImages: Boolean,
    showTopStatus: Boolean,
    showQuickChips: Boolean,
    pauseWallpaperOnStop: Boolean,
    coverStyle: CardCoverStyle,
    sampleApp: AppEntry?,
    onChangeCoverStyle: (CardCoverStyle) -> Unit,
    onApplyTouchPreset: () -> Unit,
    onUpdate: (Int, Int, Int, Int, Int, Int, Int, Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    val sliderColors = SliderDefaults.colors(
        thumbColor = Color(0xFFB388FF),
        activeTrackColor = Color(0xFF7C4DFF),
        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0x990C101B))
                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Magic Desktop 实时预览", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onApplyTouchPreset,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF651FFF))
                        ) {
                            Text("一键套用“大屏触控精简预设”", fontSize = 12.sp)
                        }
                    }
                    Text(
                        text = "23:42",
                        color = Color.White,
                        fontSize = (40 * (clockScale / 100f)).sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = if (clockTilt > 0) FontStyle.Italic else FontStyle.Normal,
                        modifier = Modifier.rotate(-clockTilt * 0.35f)
                    )
                }

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (sampleApp != null) {
                        MarcoHorizontalCard(
                            app = sampleApp,
                            displayLabel = sampleApp.defaultLabel,
                            width = cardWidth.dp,
                            height = cardHeight.dp,
                            cornerRadius = cardCorner.dp,
                            isPinned = true,
                            isWide = true,
                            showBanner = showBannerImages,
                            coverStyle = coverStyle,
                            onClick = {},
                            onLongClick = {}
                        )
                    }
                }

                Text(
                    text = "风格：${coverStyle.label} · 间距 ${cardSpacing}dp · 模糊 ${blurDp}dp · 底部 ${bottomMargin}dp",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 13.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1.05f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xD4111522))
                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(28.dp))
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("外观与布局微调", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onReset) { Text("恢复默认", color = Color.White) }
                    Button(onClick = onBack) { Text("完成") }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("卡片封面质感", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CardCoverStyle.values().toList()) { st ->
                            val selected = st == coverStyle
                            Button(
                                onClick = { onChangeCoverStyle(st) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) Color(0xFF7C4DFF) else Color.White.copy(alpha = 0.14f)
                                )
                            ) {
                                Text(st.label, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
                item {
                    Text("卡片宽度：${cardWidth} dp", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = cardWidth.toFloat(),
                        onValueChange = { onUpdate(it.toInt(), cardHeight, cardCorner, cardSpacing, bottomMargin, clockTilt, clockScale, dimPct, blurDp, showFavorites, showDate, showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop) },
                        valueRange = 130f..240f,
                        colors = sliderColors
                    )
                }
                item {
                    Text("卡片高度：${cardHeight} dp", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = cardHeight.toFloat(),
                        onValueChange = { onUpdate(cardWidth, it.toInt(), cardCorner, cardSpacing, bottomMargin, clockTilt, clockScale, dimPct, blurDp, showFavorites, showDate, showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop) },
                        valueRange = 84f..156f,
                        colors = sliderColors
                    )
                }
                item {
                    Text("卡片圆角：${cardCorner} dp", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = cardCorner.toFloat(),
                        onValueChange = { onUpdate(cardWidth, cardHeight, it.toInt(), cardSpacing, bottomMargin, clockTilt, clockScale, dimPct, blurDp, showFavorites, showDate, showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop) },
                        valueRange = 10f..44f,
                        colors = sliderColors
                    )
                }
                item {
                    Text("卡片间距：${cardSpacing} dp", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = cardSpacing.toFloat(),
                        onValueChange = { onUpdate(cardWidth, cardHeight, cardCorner, it.toInt(), bottomMargin, clockTilt, clockScale, dimPct, blurDp, showFavorites, showDate, showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop) },
                        valueRange = 8f..32f,
                        colors = sliderColors
                    )
                }
                item {
                    Text("桌面底部位置：${bottomMargin} dp", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = bottomMargin.toFloat(),
                        onValueChange = { onUpdate(cardWidth, cardHeight, cardCorner, cardSpacing, it.toInt(), clockTilt, clockScale, dimPct, blurDp, showFavorites, showDate, showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop) },
                        valueRange = 12f..96f,
                        colors = sliderColors
                    )
                }
                item {
                    Text("时钟柔圆错倾角度：${clockTilt}°（0–14°）", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = clockTilt.toFloat(),
                        onValueChange = { onUpdate(cardWidth, cardHeight, cardCorner, cardSpacing, bottomMargin, it.toInt(), clockScale, dimPct, blurDp, showFavorites, showDate, showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop) },
                        valueRange = 0f..14f,
                        colors = sliderColors
                    )
                }
                item {
                    Text("时钟字号比例：${clockScale}%", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = clockScale.toFloat(),
                        onValueChange = { onUpdate(cardWidth, cardHeight, cardCorner, cardSpacing, bottomMargin, clockTilt, it.toInt(), dimPct, blurDp, showFavorites, showDate, showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop) },
                        valueRange = 75f..130f,
                        colors = sliderColors
                    )
                }
                item {
                    Text("壁纸朦胧压暗：${dimPct}%", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = dimPct.toFloat(),
                        onValueChange = { onUpdate(cardWidth, cardHeight, cardCorner, cardSpacing, bottomMargin, clockTilt, clockScale, it.toInt(), blurDp, showFavorites, showDate, showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop) },
                        valueRange = 0f..70f,
                        colors = sliderColors
                    )
                }
                item {
                    Text("背景朦胧模糊（Android 12+）：${blurDp} dp", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = blurDp.toFloat(),
                        onValueChange = { onUpdate(cardWidth, cardHeight, cardCorner, cardSpacing, bottomMargin, clockTilt, clockScale, dimPct, it.toInt(), showFavorites, showDate, showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop) },
                        valueRange = 0f..24f,
                        colors = sliderColors
                    )
                }
                item {
                    ToggleRowItem("主屏显示“收藏”行", showFavorites) {
                        onUpdate(cardWidth, cardHeight, cardCorner, cardSpacing, bottomMargin, clockTilt, clockScale, dimPct, blurDp, !showFavorites, showDate, showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop)
                    }
                }
                item {
                    ToggleRowItem("主屏显示快捷提示条", showQuickChips) {
                        onUpdate(cardWidth, cardHeight, cardCorner, cardSpacing, bottomMargin, clockTilt, clockScale, dimPct, blurDp, showFavorites, showDate, showSecondsInSaver, showBannerImages, showTopStatus, !showQuickChips, pauseWallpaperOnStop)
                    }
                }
                item {
                    ToggleRowItem("切出桌面时暂停动态壁纸省电", pauseWallpaperOnStop) {
                        onUpdate(cardWidth, cardHeight, cardCorner, cardSpacing, bottomMargin, clockTilt, clockScale, dimPct, blurDp, showFavorites, showDate, showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, !pauseWallpaperOnStop)
                    }
                }
                item {
                    ToggleRowItem("优先使用 TV 横幅封面图", showBannerImages) {
                        onUpdate(cardWidth, cardHeight, cardCorner, cardSpacing, bottomMargin, clockTilt, clockScale, dimPct, blurDp, showFavorites, showDate, showSecondsInSaver, !showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop)
                    }
                }
                item {
                    ToggleRowItem("右上角显示日期与星期", showDate) {
                        onUpdate(cardWidth, cardHeight, cardCorner, cardSpacing, bottomMargin, clockTilt, clockScale, dimPct, blurDp, showFavorites, !showDate, showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop)
                    }
                }
                item {
                    ToggleRowItem("右上角显示 Wi-Fi 状态胶囊", showTopStatus) {
                        onUpdate(cardWidth, cardHeight, cardCorner, cardSpacing, bottomMargin, clockTilt, clockScale, dimPct, blurDp, showFavorites, showDate, showSecondsInSaver, showBannerImages, !showTopStatus, showQuickChips, pauseWallpaperOnStop)
                    }
                }
                item {
                    ToggleRowItem("放大时钟屏保显示秒数", showSecondsInSaver) {
                        onUpdate(cardWidth, cardHeight, cardCorner, cardSpacing, bottomMargin, clockTilt, clockScale, dimPct, blurDp, showFavorites, showDate, !showSecondsInSaver, showBannerImages, showTopStatus, showQuickChips, pauseWallpaperOnStop)
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleRowItem(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Button(onClick = onToggle) {
            Text(if (checked) "已开启" else "已关闭", fontSize = 12.sp)
        }
    }
}

@Composable
fun WallpapersCenterScreen(
    currentUri: String,
    officialWallpapers: List<OfficialWallpaperItem>,
    mirrorStatus: String,
    parsedVideos: List<String>,
    shaVerifyResult: String,
    onSyncMirror: () -> Unit,
    onVerifyLocalUri: (Uri) -> Unit,
    onSelectUri: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var inputUrl by remember(currentUri) { mutableStateOf(currentUri) }

    val localVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            val uriStr = uri.toString()
            inputUrl = uriStr
            onSelectUri(uriStr)
            onVerifyLocalUri(uri)
            Toast.makeText(context, "已导入本地动态壁纸并开始校验 SHA-256", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xD4101420))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("我的壁纸 · 4K 60fps 动态壁纸库与哈希校验", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(mirrorStatus, color = Color(0xFFE1BEE7), fontSize = 13.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onSyncMirror) { Text("同步壁纸库", color = Color.White) }
                Button(
                    onClick = { localVideoPicker.launch(arrayOf("video/*")) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF651FFF))
                ) {
                    Text("导入本地 MP4 并校验")
                }
                OutlinedButton(onClick = onBack) { Text("返回桌面", color = Color.White) }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(officialWallpapers, key = { it.id }) { item ->
                Column(
                    modifier = Modifier
                        .width(250.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(item.previewColorStart, item.previewColorEnd)
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(item.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(item.desc, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text(item.spec, color = Color(0xFFE1BEE7), fontSize = 11.sp)
                    Text("SHA-256: ${item.sha256.take(16)}...", color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.panUrl)))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "无法打开浏览器下载", Toast.LENGTH_SHORT).show()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("下载 4K 原片", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                inputUrl = item.defaultStreamUrl
                                onSelectUri(item.defaultStreamUrl)
                                Toast.makeText(context, "已切回循环演示片源", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("演示流", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (parsedVideos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(parsedVideos) { url ->
                    Button(onClick = {
                        inputUrl = url
                        onSelectUri(url)
                    }) {
                        Text(url.substringAfterLast('/'), fontSize = 12.sp)
                    }
                }
            }
        }

        if (shaVerifyResult.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Text(shaVerifyResult, color = Color(0xFFA5D6A7), fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                label = { Text("视频直链（MP4 / WebM / M3U8）或本地 URI") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFB388FF),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.38f),
                    focusedLabelColor = Color(0xFFE1BEE7),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.72f),
                    cursorColor = Color(0xFFB388FF)
                ),
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                onSelectUri(inputUrl)
                Toast.makeText(context, "动态壁纸已更新", Toast.LENGTH_SHORT).show()
            }) {
                Text("立即应用")
            }
        }
    }
}

@Composable
fun DualPaneSettingsScreen(
    allApps: List<AppEntry>,
    hiddenPkgs: Set<String>,
    pinnedPkgs: Set<String>,
    sortMode: SortMode,
    onSelectSortMode: (SortMode) -> Unit,
    onToggleHide: (String) -> Unit,
    onExportBackup: () -> String,
    onImportBackup: (String) -> Boolean,
    onOpenScreensaver: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(0) }
    val categories = listOf(
        "桌面与排序规则",
        "隐藏与置顶管理",
        "系统最近任务",
        "时钟与屏保",
        "本地备份与恢复",
        "设备诊断与关于"
    )
    val hiddenApps = remember(allApps, hiddenPkgs) { allApps.filter { hiddenPkgs.contains(it.packageName) } }
    var backupText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xD90E121D))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(28.dp))
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier
                .width(230.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("桌面设置", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            categories.forEachIndexed { idx, title ->
                val active = selectedCategory == idx
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) Color(0xFF7C4DFF).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.07f))
                        .clickable { selectedCategory = idx }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("返回桌面")
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedCategory) {
                0 -> {
                    Text("默认桌面与卡片排序模式", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("选择应用排序方式（所有模式均优先保留已置顶卡片）：", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SortMode.values().forEach { mode ->
                            val selected = mode == sortMode
                            Button(
                                onClick = { onSelectSortMode(mode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) Color(0xFF7C4DFF) else Color.White.copy(alpha = 0.14f)
                                )
                            ) {
                                Text(mode.label, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                            } catch (_: Exception) {
                                context.startActivity(Intent(Settings.ACTION_SETTINGS))
                            }
                        }) {
                            Text("打开系统默认桌面选择")
                        }
                        OutlinedButton(onClick = {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                        }) {
                            Text("打开系统设置", color = Color.White)
                        }
                    }
                }
                1 -> {
                    Text("已隐藏应用（${hiddenApps.size} 个）", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    if (hiddenApps.isEmpty()) {
                        Text("当前没有隐藏任何应用。在桌面长按卡片可隐藏不需要的入口。", color = Color.White.copy(alpha = 0.72f), fontSize = 14.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(hiddenApps, key = { it.packageName }) { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(app.defaultLabel, color = Color.White, fontSize = 15.sp)
                                    Button(onClick = { onToggleHide(app.packageName) }) {
                                        Text("恢复显示")
                                    }
                                }
                            }
                        }
                    }
                    Text("当前已置顶应用：${pinnedPkgs.size} 个", color = Color(0xFFE1BEE7), fontSize = 13.sp)
                }
                2 -> {
                    Text("系统最近任务与快速切换", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("联想 12.7\" 与 Portal Plus 可通过手势上滑或点击下方按钮查看后台运行状态：", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                    val memInfo = ActivityManager.MemoryInfo()
                    am?.getMemoryInfo(memInfo)
                    val availMb = memInfo.availMem / (1024 * 1024)
                    val totalMb = memInfo.totalMem / (1024 * 1024)
                    Text("当前可用内存：${availMb} MB / 总内存 ${totalMb} MB", color = Color(0xFFA5D6A7), fontSize = 15.sp)
                    Button(onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
                        } catch (_: Exception) {
                        }
                    }) {
                        Text("打开系统应用管理")
                    }
                }
                3 -> {
                    Text("放大时钟屏保（模块 11）", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("沿用 Magic Desktop 设定的时钟倾斜角度，适合平板或 Portal Plus 桌面摆件待机展示。", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Button(onClick = onOpenScreensaver) {
                        Text("立即进入放大时钟屏保")
                    }
                }
                4 -> {
                    Text("本地配置备份与恢复（JSON）", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = {
                            backupText = onExportBackup()
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cm?.setPrimaryClip(ClipData.newPlainText("MarcoTVBackup", backupText))
                            Toast.makeText(context, "已导出配置并复制到剪贴板", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("导出并复制配置")
                        }
                        OutlinedButton(onClick = {
                            val ok = onImportBackup(backupText)
                            Toast.makeText(context, if (ok) "配置已恢复生效" else "JSON 格式无效", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("从下方文本框恢复", color = Color.White)
                        }
                    }
                    OutlinedTextField(
                        value = backupText,
                        onValueChange = { backupText = it },
                        label = { Text("配置备份 JSON 内容") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFB388FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
                5 -> {
                    Text("设备诊断与关于 Marco TV Touch", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("设备型号：${Build.MANUFACTURER} ${Build.MODEL}（Android ${Build.VERSION.RELEASE} / SDK ${Build.VERSION.SDK_INT}）", color = Color.White.copy(alpha = 0.88f), fontSize = 14.sp)
                    Text("目标大屏：联想平板 12.7\" / Facebook Portal Plus（1st & 2nd Gen）", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                    Text("已识别桌面应用总数：${allApps.size} 个 | 置顶：${pinnedPkgs.size} | 隐藏：${hiddenPkgs.size}", color = Color(0xFFE1BEE7), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ScreensaverClockOverlay(
    tiltDeg: Int,
    showSeconds: Boolean,
    onDismiss: () -> Unit
) {
    var timeStr by remember { mutableStateOf("") }
    var secStr by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            secStr = SimpleDateFormat("ss", Locale.getDefault()).format(now)
            dateStr = SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINA).format(now)
            delay(500)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = timeStr,
                    color = Color.White,
                    fontSize = 138.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = if (tiltDeg > 0) FontStyle.Italic else FontStyle.Normal,
                    modifier = Modifier.rotate(-tiltDeg * 0.45f)
                )
                if (showSeconds) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = secStr,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = dateStr,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "轻触屏幕任意位置返回桌面",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 14.sp
            )
        }
    }
}
