package com.ggs.qosqoruta.features.mapa.presentation.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun ProfileAvatarImage(
    avatarUri: String?,
    avatarId: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 36.dp
) {
    val context = LocalContext.current
    var bitmapImage by remember(avatarUri) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(avatarUri) {
        if (!avatarUri.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val bmp = if (avatarUri.startsWith("http://") || avatarUri.startsWith("https://")) {
                        val url = URL(avatarUri)
                        BitmapFactory.decodeStream(url.openStream())
                    } else {
                        val uri = Uri.parse(avatarUri)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        BitmapFactory.decodeStream(inputStream)
                    }
                    bitmapImage = bmp?.asImageBitmap()
                } catch (e: Throwable) {
                    bitmapImage = null
                }
            }
        } else {
            bitmapImage = null
        }
    }

    if (bitmapImage != null) {
        Image(
            bitmap = bitmapImage!!,
            contentDescription = "Foto de Perfil",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape)
        )
    } else {
        Surface(
            shape = CircleShape,
            color = when (avatarId) {
                1 -> MaterialTheme.colorScheme.secondaryContainer
                2 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            },
            modifier = modifier
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
