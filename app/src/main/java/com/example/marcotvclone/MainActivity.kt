package com.example.marcotvclone

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable,
    val launchIntent: Intent
)

class LauncherViewModel : ViewModel() {
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps

    fun loadApps(context: Context) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val appList = resolveInfos.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null && packageName != context.packageName) {
                AppInfo(
                    label = resolveInfo.loadLabel(pm).toString(),
                    packageName = packageName,
                    icon = resolveInfo.loadIcon(pm),
                    launchIntent = launchIntent
                )
            } else null
        }.sortedBy { it.label }
        _installedApps.value = appList
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: LauncherViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            LaunchedEffect(Unit) {
                viewModel.loadApps(this@MainActivity)
            }
            MarcoTVCloneScreen(viewModel)
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoWallpaperBackground(videoUrl: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun MarcoTVCloneScreen(viewModel: LauncherViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VideoWallpaperBackground(
            videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
        )
        
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))

        TopBar(modifier = Modifier.align(Alignment.TopCenter))
        HeroSection(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 80.dp)
        )
        AppListSection(
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 48.dp, bottom = 48.dp)
        )
    }
}

@Composable
fun TopBar(modifier: Modifier = Modifier) {
    var timeText by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("☀️ MARCO TV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(48.dp))
            Text("💻 Marco 互联", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
            Spacer(modifier = Modifier.width(32.dp))
            Text("🎨 Magic Desktop", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
            Spacer(modifier = Modifier.width(32.dp))
            Text("☷ 全部应用", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Wifi, contentDescription = "WiFi", tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = timeText, color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HeroSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("MarcoTV 0.19.0", color = Color.White, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("给你的电视\n换个桌面", color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold, lineHeight = 64.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("动态壁纸 · 投屏 · 卡片自定义", color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp)
    }
}

@Composable
fun AppListSection(viewModel: LauncherViewModel, modifier: Modifier = Modifier) {
    val apps by viewModel.installedApps.collectAsState()
    val context = LocalContext.current

    Column(modifier = modifier) {
        Text(
            text = "☷ 我的应用",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                AppCardPlaceholder("Marco Movie", width = 260.dp, bgColor = Color(0xFF222222))
            }
            items(apps, key = { it.packageName }) { app ->
                AppCardReal(app = app) {
                    try {
                        context.startActivity(app.launchIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}

@Composable
fun AppCardReal(app: AppInfo, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
    
    val bitmap = remember(app.icon) {
        try {
            app.icon.toBitmap(width = 150, height = 150).asImageBitmap()
        } catch (e: Exception) {
            android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(110.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = app.label,
                modifier = Modifier.size(72.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = app.label, 
            color = Color.White, 
            fontSize = 16.sp, 
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(110.dp)
        )
    }
}

@Composable
fun AppCardPlaceholder(name: String, width: Dp, bgColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(width)
                .height(110.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text("🎬", fontSize = 40.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
