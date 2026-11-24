package com.example.marcotvclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MarcoTVCloneScreen()
        }
    }
}

@Composable
fun MarcoTVCloneScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E3C72)) 
    ) {
        TopBar(modifier = Modifier.align(Alignment.TopCenter))
        HeroSection(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 80.dp)
        )
        AppListSection(
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
fun AppListSection(modifier: Modifier = Modifier) {
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
                AppCard("Marco Movie", width = 260.dp, bgColor = Color(0xFF222222))
            }
            items(5) { index ->
                val colors = listOf(Color.White, Color(0xFFE3F2FD), Color(0xFFF3E5F5), Color(0xFFFFF3E0), Color(0xFFE8F5E9))
                AppCard("应用 ${index + 1}", width = 160.dp, bgColor = colors[index % colors.size])
            }
        }
    }
}

@Composable
fun AppCard(name: String, width: Dp, bgColor: Color) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { }
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(110.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(if (width > 200.dp) "🎬" else "📦", fontSize = 40.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
