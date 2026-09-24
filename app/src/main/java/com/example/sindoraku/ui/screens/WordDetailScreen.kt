package com.example.sindoraku.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.sindoraku.ui.components.*
import com.example.sindoraku.ui.theme.*
import com.example.sindoraku.ui.vocabulary.BisindoVocabulary

@Composable
fun WordDetailScreen(
    word: String?,
    onWordClick: (String) -> Unit
) {
    val entry = remember(word) { BisindoVocabulary.findByRouteArg(word) }
    val relatedWords = remember(entry) {
        entry?.let { BisindoVocabulary.relatedWords(it) } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSoft)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Canvas)
                .sindorakuStatusBarPadding()
                .padding(horizontal = AppTokens.Space.xl)
                .padding(top = AppTokens.Space.lg, bottom = AppTokens.Space.xl)
        ) {
            if (entry == null) {
                SindorakuTagBadge(text = "Kosakata", backgroundColor = CardTintGray, textColor = Slate)
                Spacer(Modifier.height(AppTokens.Space.sm))
                Text(
                    text = word ?: "\u2014",
                    style = MaterialTheme.typography.displayMedium,
                    color = Ink
                )
            } else {
                SindorakuTagBadge(
                    text = entry.category,
                    backgroundColor = CardTintLavender,
                    textColor = BrandPurpleDark
                )
                Spacer(Modifier.height(AppTokens.Space.sm))
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.displayMedium,
                    color = Ink
                )
                Spacer(Modifier.height(AppTokens.Space.xs))
                SindorakuCaption(text = "Kamus \u00b7 ${BisindoVocabulary.WORD_COUNT} kata")
            }
        }

        SindorakuHairlineDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTokens.Space.xl)
        ) {
            if (entry == null) {
                SindorakuSurfaceCard(backgroundColor = CardTintGray) {
                    Text(
                        text = "Kata ini tidak ada dalam kamus ${BisindoVocabulary.WORD_COUNT} kata BISINDO.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Charcoal
                    )
                }
                Spacer(Modifier.height(AppTokens.Space.section))
                return@Column
            }

            // Card tengah: demo gesture BISINDO (animated WebP, ringan).
            // Taruh file di: app/src/main/assets/gestures/<id>.webp
            GestureAnimationCard(
                gestureId = entry.id,
                label = entry.label
            )

            Spacer(Modifier.height(AppTokens.Space.xl))

            SindorakuSurfaceCard(backgroundColor = Canvas, elevated = true) {
                SindorakuCaption(text = "Makna")
                Spacer(Modifier.height(AppTokens.Space.sm))
                Text(
                    text = entry.meaning,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Charcoal
                )
            }

            Spacer(Modifier.height(AppTokens.Space.md))

            SindorakuSurfaceCard(backgroundColor = CardTintYellowBold) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(AppTokens.Space.md))
                    Column {
                        SindorakuCaption(text = "Tips AI", color = Slate)
                        Spacer(Modifier.height(AppTokens.Space.sm))
                        Text(
                            text = "Pastikan tangan terlihat jelas di kamera saat melakukan gesture \"${entry.label}\" agar AI dapat mengenali dengan akurat.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Charcoal
                        )
                    }
                }
            }

            if (relatedWords.isNotEmpty()) {
                Spacer(Modifier.height(AppTokens.Space.xl))
                SindorakuCaption(text = "Kata terkait \u00b7 ${entry.category}")
                Spacer(Modifier.height(AppTokens.Space.sm))

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppTokens.Radius.lg))
                        .background(Canvas)
                        .padding(horizontal = AppTokens.Space.md)
                ) {
                    relatedWords.forEach { related ->
                        SindorakuListRow(
                            title = related.label,
                            subtitle = related.category,
                            badgeTint = CardTintMint,
                            onClick = { onWordClick(related.id) }
                        )
                        if (related != relatedWords.last()) {
                            SindorakuHairlineDivider()
                        }
                    }
                }
            }

            Spacer(Modifier.height(AppTokens.Space.section))
        }
    }
}

/**
 * Menampilkan animasi gesture BISINDO sebagai animated WebP (ringan).
 *
 * File animasi diletakkan di: app/src/main/assets/gestures/<gestureId>.webp
 * Kalau file belum ada / gagal dimuat, otomatis fallback ke ikon placeholder
 * (tampilan lama), jadi aman meski belum semua kata punya animasi.
 *
 * Kenapa WebP animasi, bukan MP4:
 * - Ukuran jauh lebih kecil (bisa < 150 KB per gesture)
 * - Loop otomatis, tanpa perlu media player berat (hemat memori)
 * - Cukup di-decode Coil, tidak perlu release() manual
 */
@Composable
private fun GestureAnimationCard(
    gestureId: String,
    label: String
) {
    val context = LocalContext.current

    // ImageLoader dengan decoder animasi (WebP/GIF).
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(AppTokens.Radius.lg))
            .background(CardTintSky),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                // Ganti ke URL server kalau mau streaming + cache otomatis,
                // misalnya: .data("https://cdn.contoh.com/gestures/$gestureId.webp")
                .data("file:///android_asset/gestures/$gestureId.webp")
                .crossfade(true)
                .build(),
            imageLoader = imageLoader,
            contentDescription = "Animasi gesture BISINDO $label",
            contentScale = ContentScale.Fit,
            loading = { GesturePlaceholder(label = label) },
            error = { GesturePlaceholder(label = label) },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/** Tampilan fallback (sama seperti versi lama) saat animasi loading / tidak ada. */
@Composable
private fun GesturePlaceholder(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.PanTool,
            contentDescription = null,
            tint = LinkBlue,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(AppTokens.Space.md))
        Text(
            text = label,
            style = MaterialTheme.typography.headlineLarge,
            color = Ink
        )
        Spacer(Modifier.height(AppTokens.Space.xs))
        SindorakuCaption(text = "Gesture BISINDO", color = Steel)
    }
}
