package com.example.marcotvclone

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale

class MarcoViewModel : ViewModel() {
    private val prefsName = "marcotv_0213_nextgen_prefs"
    private val keyPinned = "pinned_pkgs"
    private val keyHidden = "hidden_pkgs"
    private val keyFavorites = "favorite_pkgs"
    private val keyWideCards = "wide_card_pkgs"
    private val keyCustomOrder = "custom_order_list"
    private val keyLastOpened = "last_opened_json"
    private val keySortMode = "sort_mode"
    private val keyCardCoverStyle = "card_cover_style"

    private val keyCardWidth = "card_width_dp"
    private val keyCardHeight = "card_height_dp"
    private val keyCardCorner = "card_corner_dp"
    private val keyCardSpacing = "card_spacing_dp"
    private val keyBottomMargin = "bottom_margin_dp"
    private val keyClockTilt = "clock_tilt_deg"
    private val keyClockScale = "clock_scale_pct"
    private val keyShowDate = "show_date"
    private val keyShowSecondsInSaver = "show_seconds_saver"
    private val keyShowFavoritesRow = "show_favorites_row"
    private val keyShowBannerImages = "show_banner_images"
    private val keyWallpaperUri = "active_wallpaper_uri"
    private val keyDimAlpha = "wallpaper_dim_pct"
    private val keyBlurRadius = "wallpaper_blur_dp"
    private val keyShowTopStatus = "show_top_status"
    private val keyShowQuickChips = "show_quick_chips"
    private val keyPauseOnStop = "pause_wallpaper_on_stop"

    val defaultDemoStream = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"

    private val _allApps = MutableStateFlow<List<AppEntry>>(emptyList())
    val allApps: StateFlow<List<AppEntry>> = _allApps

    private val _pinnedPackages = MutableStateFlow<Set<String>>(emptySet())
    val pinnedPackages: StateFlow<Set<String>> = _pinnedPackages

    private val _hiddenPackages = MutableStateFlow<Set<String>>(emptySet())
    val hiddenPackages: StateFlow<Set<String>> = _hiddenPackages

    private val _favoritePackages = MutableStateFlow<Set<String>>(emptySet())
    val favoritePackages: StateFlow<Set<String>> = _favoritePackages

    private val _wideCardPackages = MutableStateFlow<Set<String>>(emptySet())
    val wideCardPackages: StateFlow<Set<String>> = _wideCardPackages

    private val _customOrder = MutableStateFlow<List<String>>(emptyList())
    val customOrder: StateFlow<List<String>> = _customOrder

    private val _lastOpenedMap = MutableStateFlow<Map<String, Long>>(emptyMap())
    val lastOpenedMap: StateFlow<Map<String, Long>> = _lastOpenedMap

    private val _sortMode = MutableStateFlow(SortMode.RECENT)
    val sortMode: StateFlow<SortMode> = _sortMode

    private val _cardCoverStyle = MutableStateFlow(CardCoverStyle.DEFAULT_GRADIENT)
    val cardCoverStyle: StateFlow<CardCoverStyle> = _cardCoverStyle

    private val _customNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val customNames: StateFlow<Map<String, String>> = _customNames

    private val _activeScreen = MutableStateFlow(ActiveScreen.DESKTOP)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen

    private val _cardWidthDp = MutableStateFlow(172)
    val cardWidthDp: StateFlow<Int> = _cardWidthDp

    private val _cardHeightDp = MutableStateFlow(104)
    val cardHeightDp: StateFlow<Int> = _cardHeightDp

    private val _cardCornerDp = MutableStateFlow(24)
    val cardCornerDp: StateFlow<Int> = _cardCornerDp

    private val _cardSpacingDp = MutableStateFlow(16)
    val cardSpacingDp: StateFlow<Int> = _cardSpacingDp

    private val _bottomMarginDp = MutableStateFlow(28)
    val bottomMarginDp: StateFlow<Int> = _bottomMarginDp

    private val _clockTiltDeg = MutableStateFlow(6)
    val clockTiltDeg: StateFlow<Int> = _clockTiltDeg

    private val _clockScalePct = MutableStateFlow(100)
    val clockScalePct: StateFlow<Int> = _clockScalePct

    private val _showDate = MutableStateFlow(true)
    val showDate: StateFlow<Boolean> = _showDate

    private val _showSecondsInSaver = MutableStateFlow(true)
    val showSecondsInSaver: StateFlow<Boolean> = _showSecondsInSaver

    private val _showFavoritesRow = MutableStateFlow(true)
    val showFavoritesRow: StateFlow<Boolean> = _showFavoritesRow

    private val _showBannerImages = MutableStateFlow(true)
    val showBannerImages: StateFlow<Boolean> = _showBannerImages

    private val _showTopStatus = MutableStateFlow(true)
    val showTopStatus: StateFlow<Boolean> = _showTopStatus

    private val _showQuickChips = MutableStateFlow(true)
    val showQuickChips: StateFlow<Boolean> = _showQuickChips

    private val _pauseWallpaperOnStop = MutableStateFlow(true)
    val pauseWallpaperOnStop: StateFlow<Boolean> = _pauseWallpaperOnStop

    private val _wallpaperUri = MutableStateFlow(defaultDemoStream)
    val wallpaperUri: StateFlow<String> = _wallpaperUri

    private val _wallpaperDimPct = MutableStateFlow(28)
    val wallpaperDimPct: StateFlow<Int> = _wallpaperDimPct

    private val _wallpaperBlurDp = MutableStateFlow(0)
    val wallpaperBlurDp: StateFlow<Int> = _wallpaperBlurDp

    private val _onlineMirrorStatus = MutableStateFlow("点击同步壁纸库可解析镜像列表")
    val onlineMirrorStatus: StateFlow<String> = _onlineMirrorStatus

    private val _parsedDirectVideos = MutableStateFlow<List<String>>(emptyList())
    val parsedDirectVideos: StateFlow<List<String>> = _parsedDirectVideos

    private val _shaVerifyResult = MutableStateFlow("")
    val shaVerifyResult: StateFlow<String> = _shaVerifyResult

    val officialWallpapers = listOf(
        OfficialWallpaperItem(
            id = "penguin",
            title = "害羞小企鹅",
            spec = "3840×2160 · 60fps · 20s · 7.6 MB · HEVC",
            desc = "Q版皮克斯 · 暖灯沙发捂眼微动态",
            sha256 = "2e3ad375c721e0e2216b010324a18d9ef98875f73e1d47cd855c98f3c3650638",
            panUrl = "https://pan.quark.cn/s/f0311a0a6951",
            previewColorStart = Color(0xFF1A2A4A),
            previewColorEnd = Color(0xFF4A3525),
            defaultStreamUrl = defaultDemoStream
        ),
        OfficialWallpaperItem(
            id = "squirrel",
            title = "顽皮小松鼠",
            spec = "3840×2160 · 60fps · 20s · 9.0 MB · HEVC",
            desc = "Q版皮克斯 · 蓝调沙发探头微动态",
            sha256 = "913f791258e21e4ffb89f60c8cb9aaf58b519941e771c20a94d29b26894e7f61",
            panUrl = "https://pan.quark.cn/s/b5f19457e16d",
            previewColorStart = Color(0xFF102A43),
            previewColorEnd = Color(0xFF243B53),
            defaultStreamUrl = defaultDemoStream
        ),
        OfficialWallpaperItem(
            id = "puppy",
            title = "招手小奶狗",
            spec = "3840×2160 · 60fps · 20s · 9.5 MB · HEVC",
            desc = "Q版皮克斯 · 暖米沙发卖萌微动态",
            sha256 = "caffca8b6c1cb4cb639ccc52c23c18f60a95fb7239ca0c9b89691ef5986f2740",
            panUrl = "https://pan.quark.cn/s/b8b083ae9048",
            previewColorStart = Color(0xFF3E2723),
            previewColorEnd = Color(0xFF5D4037),
            defaultStreamUrl = defaultDemoStream
        ),
        OfficialWallpaperItem(
            id = "blackcat",
            title = "乖巧小黑猫",
            spec = "3840×2160 · 60fps · 20s · 9.8 MB · HEVC",
            desc = "Q版皮克斯 · 绿墙木柜探头微动态",
            sha256 = "d4e69b4acbc4172aa5a500bcd66836fbc0f66e863fc1fff171a416b303b7ae10",
            panUrl = "https://pan.quark.cn/s/2b465cf96656",
            previewColorStart = Color(0xFF1B4332),
            previewColorEnd = Color(0xFF2D6A4F),
            defaultStreamUrl = defaultDemoStream
        ),
        OfficialWallpaperItem(
            id = "porkbelly",
            title = "作者的五花肉~",
            spec = "3840×2160 · 60fps · 20s · 17.0 MB · HEVC",
            desc = "Q版皮克斯 · 暖灯沙发舔爪微动态",
            sha256 = "6c4e0afbed3b095e3774366ebebd9f2e67411cba17ae82f0b221d42288f54d81",
            panUrl = "https://pan.quark.cn/s/b29e936a3089",
            previewColorStart = Color(0xFF3B1E2B),
            previewColorEnd = Color(0xFF5C2C3E),
            defaultStreamUrl = defaultDemoStream
        )
    )

    fun loadState(context: Context) {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        _pinnedPackages.value = prefs.getStringSet(keyPinned, emptySet())?.toSet() ?: emptySet()
        _hiddenPackages.value = prefs.getStringSet(keyHidden, emptySet())?.toSet() ?: emptySet()
        _favoritePackages.value = prefs.getStringSet(keyFavorites, emptySet())?.toSet() ?: emptySet()
        _wideCardPackages.value = prefs.getStringSet(keyWideCards, emptySet())?.toSet() ?: emptySet()

        val orderStr = prefs.getString(keyCustomOrder, "") ?: ""
        _customOrder.value = if (orderStr.isBlank()) emptyList() else orderStr.split(",").filter { it.isNotBlank() }

        val openedJson = prefs.getString(keyLastOpened, "{}") ?: "{}"
        val openedMap = mutableMapOf<String, Long>()
        try {
            val obj = JSONObject(openedJson)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                openedMap[k] = obj.optLong(k, 0L)
            }
        } catch (_: Exception) {
        }
        _lastOpenedMap.value = openedMap

        val sortName = prefs.getString(keySortMode, SortMode.RECENT.name) ?: SortMode.RECENT.name
        _sortMode.value = try { SortMode.valueOf(sortName) } catch (_: Exception) { SortMode.RECENT }

        val coverName = prefs.getString(keyCardCoverStyle, CardCoverStyle.DEFAULT_GRADIENT.name) ?: CardCoverStyle.DEFAULT_GRADIENT.name
        _cardCoverStyle.value = try { CardCoverStyle.valueOf(coverName) } catch (_: Exception) { CardCoverStyle.DEFAULT_GRADIENT }

        _cardWidthDp.value = prefs.getInt(keyCardWidth, 172).coerceIn(130, 240)
        _cardHeightDp.value = prefs.getInt(keyCardHeight, 104).coerceIn(84, 156)
        _cardCornerDp.value = prefs.getInt(keyCardCorner, 24).coerceIn(10, 44)
        _cardSpacingDp.value = prefs.getInt(keyCardSpacing, 16).coerceIn(8, 32)
        _bottomMarginDp.value = prefs.getInt(keyBottomMargin, 28).coerceIn(12, 96)
        _clockTiltDeg.value = prefs.getInt(keyClockTilt, 6).coerceIn(0, 14)
        _clockScalePct.value = prefs.getInt(keyClockScale, 100).coerceIn(75, 130)
        _showDate.value = prefs.getBoolean(keyShowDate, true)
        _showSecondsInSaver.value = prefs.getBoolean(keyShowSecondsInSaver, true)
        _showFavoritesRow.value = prefs.getBoolean(keyShowFavoritesRow, true)
        _showBannerImages.value = prefs.getBoolean(keyShowBannerImages, true)
        _showTopStatus.value = prefs.getBoolean(keyShowTopStatus, true)
        _showQuickChips.value = prefs.getBoolean(keyShowQuickChips, true)
        _pauseWallpaperOnStop.value = prefs.getBoolean(keyPauseOnStop, true)
        _wallpaperDimPct.value = prefs.getInt(keyDimAlpha, 28).coerceIn(0, 70)
        _wallpaperBlurDp.value = prefs.getInt(keyBlurRadius, 0).coerceIn(0, 24)

        val savedUri = prefs.getString(keyWallpaperUri, defaultDemoStream)
        if (!savedUri.isNullOrBlank()) {
            _wallpaperUri.value = savedUri
        }
    }

    fun navigateTo(screen: ActiveScreen) {
        _activeScreen.value = screen
    }

    fun setSortMode(context: Context, mode: SortMode) {
        _sortMode.value = mode
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
            .putString(keySortMode, mode.name)
            .apply()
    }

    fun setCardCoverStyle(context: Context, style: CardCoverStyle) {
        _cardCoverStyle.value = style
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
            .putString(keyCardCoverStyle, style.name)
            .apply()
    }

    fun recordAppLaunched(context: Context, pkg: String) {
        val updated = _lastOpenedMap.value.toMutableMap()
        updated[pkg] = System.currentTimeMillis()
        _lastOpenedMap.value = updated.toMap()
        val json = JSONObject()
        updated.forEach { (k, v) -> json.put(k, v) }
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
            .putString(keyLastOpened, json.toString())
            .apply()
    }

    fun moveAppInCustomOrder(context: Context, pkg: String, delta: Int, currentOrderedPkgs: List<String>) {
        val list = if (_customOrder.value.isEmpty()) currentOrderedPkgs.toMutableList() else _customOrder.value.toMutableList()
        currentOrderedPkgs.forEach { if (!list.contains(it)) list.add(it) }
        val idx = list.indexOf(pkg)
        if (idx >= 0) {
            val targetIdx = (idx + delta).coerceIn(0, list.lastIndex)
            if (targetIdx != idx) {
                val item = list.removeAt(idx)
                list.add(targetIdx, item)
                _customOrder.value = list.toList()
                _sortMode.value = SortMode.CUSTOM
                context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
                    .putString(keyCustomOrder, list.joinToString(","))
                    .putString(keySortMode, SortMode.CUSTOM.name)
                    .apply()
            }
        }
    }

    fun updateMagicAppearance(
        context: Context,
        widthDp: Int = _cardWidthDp.value,
        heightDp: Int = _cardHeightDp.value,
        cornerDp: Int = _cardCornerDp.value,
        spacingDp: Int = _cardSpacingDp.value,
        bottomDp: Int = _bottomMarginDp.value,
        tiltDeg: Int = _clockTiltDeg.value,
        scalePct: Int = _clockScalePct.value,
        dimPct: Int = _wallpaperDimPct.value,
        blurDp: Int = _wallpaperBlurDp.value,
        showFav: Boolean = _showFavoritesRow.value,
        showDateFlag: Boolean = _showDate.value,
        showSecFlag: Boolean = _showSecondsInSaver.value,
        showBannerFlag: Boolean = _showBannerImages.value,
        showStatusFlag: Boolean = _showTopStatus.value,
        showChipsFlag: Boolean = _showQuickChips.value,
        pauseOnStopFlag: Boolean = _pauseWallpaperOnStop.value
    ) {
        _cardWidthDp.value = widthDp.coerceIn(130, 240)
        _cardHeightDp.value = heightDp.coerceIn(84, 156)
        _cardCornerDp.value = cornerDp.coerceIn(10, 44)
        _cardSpacingDp.value = spacingDp.coerceIn(8, 32)
        _bottomMarginDp.value = bottomDp.coerceIn(12, 96)
        _clockTiltDeg.value = tiltDeg.coerceIn(0, 14)
        _clockScalePct.value = scalePct.coerceIn(75, 130)
        _wallpaperDimPct.value = dimPct.coerceIn(0, 70)
        _wallpaperBlurDp.value = blurDp.coerceIn(0, 24)
        _showFavoritesRow.value = showFav
        _showDate.value = showDateFlag
        _showSecondsInSaver.value = showSecFlag
        _showBannerImages.value = showBannerFlag
        _showTopStatus.value = showStatusFlag
        _showQuickChips.value = showChipsFlag
        _pauseWallpaperOnStop.value = pauseOnStopFlag

        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
            .putInt(keyCardWidth, _cardWidthDp.value)
            .putInt(keyCardHeight, _cardHeightDp.value)
            .putInt(keyCardCorner, _cardCornerDp.value)
            .putInt(keyCardSpacing, _cardSpacingDp.value)
            .putInt(keyBottomMargin, _bottomMarginDp.value)
            .putInt(keyClockTilt, _clockTiltDeg.value)
            .putInt(keyClockScale, _clockScalePct.value)
            .putInt(keyDimAlpha, _wallpaperDimPct.value)
            .putInt(keyBlurRadius, _wallpaperBlurDp.value)
            .putBoolean(keyShowFavoritesRow, _showFavoritesRow.value)
            .putBoolean(keyShowDate, _showDate.value)
            .putBoolean(keyShowSecondsInSaver, _showSecondsInSaver.value)
            .putBoolean(keyShowBannerImages, _showBannerImages.value)
            .putBoolean(keyShowTopStatus, _showTopStatus.value)
            .putBoolean(keyShowQuickChips, _showQuickChips.value)
            .putBoolean(keyPauseOnStop, _pauseWallpaperOnStop.value)
            .apply()
    }

    fun applyTouchConsolePreset(context: Context) {
        setCardCoverStyle(context, CardCoverStyle.FROSTED_GLASS)
        updateMagicAppearance(
            context = context,
            widthDp = 180,
            heightDp = 108,
            cornerDp = 26,
            spacingDp = 18,
            bottomDp = 32,
            tiltDeg = 4,
            scalePct = 104,
            dimPct = 32,
            blurDp = 0,
            showFav = true,
            showDateFlag = true,
            showSecFlag = true,
            showBannerFlag = true,
            showStatusFlag = true,
            showChipsFlag = false,
            pauseOnStopFlag = true
        )
    }

    fun resetMagicAppearance(context: Context) {
        setCardCoverStyle(context, CardCoverStyle.DEFAULT_GRADIENT)
        updateMagicAppearance(
            context = context,
            widthDp = 172,
            heightDp = 104,
            cornerDp = 24,
            spacingDp = 16,
            bottomDp = 28,
            tiltDeg = 6,
            scalePct = 100,
            dimPct = 28,
            blurDp = 0,
            showFav = true,
            showDateFlag = true,
            showSecFlag = true,
            showBannerFlag = true,
            showStatusFlag = true,
            showChipsFlag = true,
            pauseOnStopFlag = true
        )
    }

    fun applyWallpaper(context: Context, uriString: String) {
        val clean = uriString.trim()
        if (clean.isNotEmpty()) {
            _wallpaperUri.value = clean
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
                .putString(keyWallpaperUri, clean)
                .apply()
        }
    }

    fun syncWallpaperMirror() {
        viewModelScope.launch {
            _onlineMirrorStatus.value = "正在连接 marcotv.wlj9919.top/wallpapers/ ..."
            try {
                val result = withContext(Dispatchers.IO) {
                    val conn = URL("https://marcotv.wlj9919.top/wallpapers/").openConnection() as HttpURLConnection
                    conn.connectTimeout = 6000
                    conn.readTimeout = 6000
                    conn.requestMethod = "GET"
                    val html = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    val regex = Regex("""https?://[^\s"'<>]+\.(mp4|webm|m3u8)""", RegexOption.IGNORE_CASE)
                    regex.findAll(html).map { it.value }.distinct().toList()
                }
                _parsedDirectVideos.value = result
                _onlineMirrorStatus.value = if (result.isEmpty()) {
                    "已同步壁纸库目录（5 款官方 4K 壁纸均通过网盘分发，下载后点击上方导入即可）"
                } else {
                    "解析到 ${result.size} 个在线视频直链，可直接点击应用"
                }
            } catch (e: Exception) {
                _onlineMirrorStatus.value = "镜像同步失败：${e.message ?: "网络不可用"}"
            }
        }
    }

    fun verifyLocalVideoSha256(context: Context, uri: Uri) {
        viewModelScope.launch {
            _shaVerifyResult.value = "正在计算本地文件 SHA-256..."
            val text = withContext(Dispatchers.IO) {
                try {
                    var fileName = "未知文件"
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0 && cursor.moveToFirst()) {
                            fileName = cursor.getString(idx)
                        }
                    }
                    val digest = MessageDigest.getInstance("SHA-256")
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val buf = ByteArray(8192)
                        var read: Int
                        while (stream.read(buf).also { read = it } != -1) {
                            digest.update(buf, 0, read)
                        }
                    }
                    val hex = digest.digest().joinToString("") { "%02x".format(it) }
                    val matched = officialWallpapers.find { it.sha256.equals(hex, ignoreCase = true) }
                    if (matched != null) {
                        "校验通过！匹配官方原片【${matched.title}】（$fileName）"
                    } else {
                        "文件：$fileName | SHA-256：${hex.take(24)}...（未匹配 5 款官方哈希，属于自定义壁纸）"
                    }
                } catch (e: Exception) {
                    "校验失败：${e.message}"
                }
            }
            _shaVerifyResult.value = text
        }
    }

    fun exportBackupJson(context: Context): String {
        val root = JSONObject()
        root.put("version", "0.21.3-touch-nextgen")
        root.put("sortMode", _sortMode.value.name)
        root.put("coverStyle", _cardCoverStyle.value.name)
        root.put("cardWidth", _cardWidthDp.value)
        root.put("cardHeight", _cardHeightDp.value)
        root.put("cardCorner", _cardCornerDp.value)
        root.put("cardSpacing", _cardSpacingDp.value)
        root.put("bottomMargin", _bottomMarginDp.value)
        root.put("clockTilt", _clockTiltDeg.value)
        root.put("clockScale", _clockScalePct.value)
        root.put("dimPct", _wallpaperDimPct.value)
        root.put("blurDp", _wallpaperBlurDp.value)
        root.put("showFavorites", _showFavoritesRow.value)
        root.put("showQuickChips", _showQuickChips.value)
        root.put("pauseWallpaperOnStop", _pauseWallpaperOnStop.value)
        root.put("wallpaperUri", _wallpaperUri.value)
        root.put("pinned", JSONArray(_pinnedPackages.value.toList()))
        root.put("hidden", JSONArray(_hiddenPackages.value.toList()))
        root.put("favorites", JSONArray(_favoritePackages.value.toList()))
        root.put("wideCards", JSONArray(_wideCardPackages.value.toList()))
        root.put("customOrder", JSONArray(_customOrder.value))
        val namesObj = JSONObject()
        _customNames.value.forEach { (k, v) -> namesObj.put(k, v) }
        root.put("customNames", namesObj)
        return root.toString(2)
    }

    fun importBackupJson(context: Context, jsonStr: String): Boolean {
        return try {
            val obj = JSONObject(jsonStr)
            val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()

            fun readStringSet(key: String): Set<String> {
                val arr = obj.optJSONArray(key) ?: return emptySet()
                return (0 until arr.length()).map { arr.optString(it) }.filter { it.isNotBlank() }.toSet()
            }

            val pinned = readStringSet("pinned")
            val hidden = readStringSet("hidden")
            val favs = readStringSet("favorites")
            val wide = readStringSet("wideCards")
            val orderArr = obj.optJSONArray("customOrder")
            val orderList = if (orderArr != null) {
                (0 until orderArr.length()).map { orderArr.optString(it) }.filter { it.isNotBlank() }
            } else emptyList()

            val namesObj = obj.optJSONObject("customNames")
            val namesMap = mutableMapOf<String, String>()
            if (namesObj != null) {
                val keys = namesObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val v = namesObj.optString(k)
                    if (v.isNotBlank()) {
                        namesMap[k] = v
                        prefs.putString("rename_$k", v)
                    }
                }
            }

            _pinnedPackages.value = pinned
            _hiddenPackages.value = hidden
            _favoritePackages.value = favs
            _wideCardPackages.value = wide
            _customOrder.value = orderList
            _customNames.value = namesMap

            prefs.putStringSet(keyPinned, pinned)
            prefs.putStringSet(keyHidden, hidden)
            prefs.putStringSet(keyFavorites, favs)
            prefs.putStringSet(keyWideCards, wide)
            prefs.putString(keyCustomOrder, orderList.joinToString(","))

            val sort = try { SortMode.valueOf(obj.optString("sortMode", SortMode.RECENT.name)) } catch (_: Exception) { SortMode.RECENT }
            val cover = try { CardCoverStyle.valueOf(obj.optString("coverStyle", CardCoverStyle.DEFAULT_GRADIENT.name)) } catch (_: Exception) { CardCoverStyle.DEFAULT_GRADIENT }
            setSortMode(context, sort)
            setCardCoverStyle(context, cover)

            updateMagicAppearance(
                context = context,
                widthDp = obj.optInt("cardWidth", 172),
                heightDp = obj.optInt("cardHeight", 104),
                cornerDp = obj.optInt("cardCorner", 24),
                spacingDp = obj.optInt("cardSpacing", 16),
                bottomDp = obj.optInt("bottomMargin", 28),
                tiltDeg = obj.optInt("clockTilt", 6),
                scalePct = obj.optInt("clockScale", 100),
                dimPct = obj.optInt("dimPct", 28),
                blurDp = obj.optInt("blurDp", 0),
                showFav = obj.optBoolean("showFavorites", true),
                showChipsFlag = obj.optBoolean("showQuickChips", true),
                pauseOnStopFlag = obj.optBoolean("pauseWallpaperOnStop", true)
            )
            val wp = obj.optString("wallpaperUri", defaultDemoStream)
            if (wp.isNotBlank()) applyWallpaper(context, wp)
            prefs.apply()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun togglePinned(context: Context, pkg: String) {
        val set = _pinnedPackages.value.toMutableSet()
        if (!set.add(pkg)) set.remove(pkg)
        _pinnedPackages.value = set.toSet()
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
            .putStringSet(keyPinned, _pinnedPackages.value)
            .apply()
    }

    fun toggleHidden(context: Context, pkg: String) {
        val set = _hiddenPackages.value.toMutableSet()
        if (!set.add(pkg)) set.remove(pkg)
        _hiddenPackages.value = set.toSet()
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
            .putStringSet(keyHidden, _hiddenPackages.value)
            .apply()
    }

    fun toggleFavorite(context: Context, pkg: String) {
        val set = _favoritePackages.value.toMutableSet()
        if (!set.add(pkg)) set.remove(pkg)
        _favoritePackages.value = set.toSet()
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
            .putStringSet(keyFavorites, _favoritePackages.value)
            .apply()
    }

    fun toggleWideCard(context: Context, pkg: String) {
        val set = _wideCardPackages.value.toMutableSet()
        if (!set.add(pkg)) set.remove(pkg)
        _wideCardPackages.value = set.toSet()
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
            .putStringSet(keyWideCards, _wideCardPackages.value)
            .apply()
    }

    fun renameApp(context: Context, pkg: String, newLabel: String) {
        val map = _customNames.value.toMutableMap()
        val trimmed = newLabel.trim().take(32)
        if (trimmed.isEmpty()) {
            map.remove(pkg)
        } else {
            map[pkg] = trimmed
        }
        _customNames.value = map.toMap()
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit()
            .putString("rename_$pkg", trimmed)
            .apply()
    }

    fun displayLabel(app: AppEntry): String {
        return _customNames.value[app.packageName]?.takeIf { it.isNotBlank() } ?: app.defaultLabel
    }

    fun loadInstalledApps(context: Context) {
        val pm = context.packageManager
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
        val palette = listOf(
            Color(0xFF1D4ED8),
            Color(0xFF0E7490),
            Color(0xFF6D28D9),
            Color(0xFFB91C1C),
            Color(0xFF047857),
            Color(0xFF4338CA)
        )

        val renameMap = mutableMapOf<String, String>()
        val entries = resolveInfos.mapNotNull { info ->
            val pkg = info.activityInfo.packageName
            val launch = pm.getLaunchIntentForPackage(pkg)
            if (launch != null && pkg != context.packageName) {
                val savedName = prefs.getString("rename_$pkg", null)
                if (!savedName.isNullOrBlank()) {
                    renameMap[pkg] = savedName
                }
                val iconBmp = drawableToBitmap(info.loadIcon(pm), 160, 160).asImageBitmap()
                val bannerDrawable = try {
                    info.activityInfo.loadBanner(pm) ?: pm.getApplicationBanner(pkg)
                } catch (_: Exception) {
                    null
                }
                val bannerBmp = bannerDrawable?.let { drawableToBitmap(it, 320, 180).asImageBitmap() }
                val color = palette[kotlin.math.abs(pkg.hashCode()) % palette.size]
                AppEntry(
                    defaultLabel = info.loadLabel(pm).toString(),
                    packageName = pkg,
                    iconBitmap = iconBmp,
                    bannerBitmap = bannerBmp,
                    launchIntent = launch,
                    accentColor = color
                )
            } else null
        }.distinctBy { it.packageName }.sortedBy { it.defaultLabel.lowercase(Locale.getDefault()) }

        _customNames.value = renameMap
        _allApps.value = entries

        if (_favoritePackages.value.isEmpty() && entries.isNotEmpty()) {
            val initialFavs = entries.take(5).map { it.packageName }.toSet()
            _favoritePackages.value = initialFavs
            prefs.edit().putStringSet(keyFavorites, initialFavs).apply()
        }
        if (_wideCardPackages.value.isEmpty() && entries.isNotEmpty()) {
            val initialWide = entries.take(2).map { it.packageName }.toSet()
            _wideCardPackages.value = initialWide
            prefs.edit().putStringSet(keyWideCards, initialWide).apply()
        }
    }

    private fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        } else {
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
        return bitmap
    }
}
