package com.vvit.smartcollege.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vvit.smartcollege.ui.theme.PrimaryNavy
import com.vvit.smartcollege.ui.theme.Typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

// Cache downloaded bitmaps in memory to prevent refetching on every recomposition
private val avatarBitmapCache = mutableMapOf<String, ImageBitmap>()

@Composable
fun StudentAvatar(
    photoUrl: String?,
    name: String,
    size: Dp = 54.dp,
    textStyle: TextStyle = Typography.headlineSmall,
    backgroundColor: Color = PrimaryNavy,
    modifier: Modifier = Modifier
) {
    val cleanUrl = remember(photoUrl) {
        photoUrl?.trim()?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
    }

    var imageBitmap by remember(cleanUrl) {
        mutableStateOf(cleanUrl?.let { avatarBitmapCache[it] })
    }

    LaunchedEffect(cleanUrl) {
        if (cleanUrl != null && imageBitmap == null) {
            val cached = avatarBitmapCache[cleanUrl]
            if (cached != null) {
                imageBitmap = cached
            } else {
                val bmp = withContext(Dispatchers.IO) {
                    downloadBitmapFromUrl(cleanUrl)
                }
                if (bmp != null) {
                    val converted = bmp.asImageBitmap()
                    avatarBitmapCache[cleanUrl] = converted
                    imageBitmap = converted
                }
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = "Student Photo",
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getInitials(name),
                style = textStyle.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

private fun downloadBitmapFromUrl(urlString: String): android.graphics.Bitmap? {
    var conn: HttpURLConnection? = null
    try {
        var currentUrl = urlString
        var redirects = 0
        while (redirects < 5) {
            val url = URL(currentUrl)
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 10000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36")
                setRequestProperty("Accept", "image/*,*/*")
                doInput = true
            }
            conn.connect()
            val code = conn.responseCode
            if (code in 300..399) {
                val loc = conn.getHeaderField("Location")
                conn.disconnect()
                if (!loc.isNullOrBlank()) {
                    currentUrl = loc
                    redirects++
                    continue
                }
                break
            }
            if (code in 200..299) {
                val bytes = conn.inputStream.use { it.readBytes() }
                conn.disconnect()
                if (bytes.isNotEmpty()) {
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bmp != null) {
                        android.util.Log.d("StudentAvatar", "Loaded avatar (${bytes.size} bytes) from: $urlString")
                        return bmp
                    }
                }
            } else {
                android.util.Log.w("StudentAvatar", "HTTP $code fetching avatar from: $urlString")
            }
            break
        }
    } catch (e: Exception) {
        android.util.Log.e("StudentAvatar", "Failed downloading avatar from $urlString: ${e.message}")
    } finally {
        try { conn?.disconnect() } catch (ignored: Exception) {}
    }
    return null
}

fun getInitials(name: String): String {
    val clean = name.trim()
    if (clean.isBlank()) return "TC"
    val parts = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
    val filtered = parts.filter { 
        val lower = it.lowercase().trimEnd('.')
        lower !in setOf("dr", "prof", "mr", "ms", "mrs", "er")
    }
    val effective = if (filtered.isNotEmpty()) filtered else parts
    return when {
        effective.size >= 2 -> "${effective[0].first().uppercaseChar()}${effective[1].first().uppercaseChar()}"
        effective[0].length >= 2 -> effective[0].substring(0, 2).uppercase()
        effective.isNotEmpty() -> effective[0].first().uppercaseChar().toString()
        else -> "TC"
    }
}

