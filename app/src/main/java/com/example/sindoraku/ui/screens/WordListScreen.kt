package com.example.sindoraku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import com.example.sindoraku.ui.components.*
import com.example.sindoraku.ui.theme.*
import com.example.sindoraku.ui.vocabulary.BisindoVocabulary

@Composable
fun WordListScreen(navController: NavController) {

    var selectedCategory by remember { mutableStateOf("Semua") }
    var search by remember { mutableStateOf("") }

    val filteredWords = remember(selectedCategory, search) {
        BisindoVocabulary.filter(selectedCategory, search)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Canvas)
                .sindorakuStatusBarPadding()
                .padding(horizontal = AppTokens.Space.xl)
                .padding(top = AppTokens.Space.lg, bottom = AppTokens.Space.xl)
        ) {
            SindorakuSectionTitle(
                title = "Kamus BISINDO",
                subtitle = if (search.isBlank() && selectedCategory == "Semua") {
                    "${BisindoVocabulary.WORD_COUNT} kata dasar"
                } else {
                    "${filteredWords.size} dari ${BisindoVocabulary.WORD_COUNT} kata"
                }
            )

            Spacer(Modifier.height(AppTokens.Space.xl))

            SindorakuSearchField(
                value = search,
                onValueChange = { search = it },
                placeholder = "Cari dari ${BisindoVocabulary.WORD_COUNT} kata..."
            )
        }

        SindorakuHairlineDivider()

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Canvas)
                .padding(horizontal = AppTokens.Space.xl, vertical = AppTokens.Space.md),
            horizontalArrangement = Arrangement.spacedBy(AppTokens.Space.sm)
        ) {
            items(BisindoVocabulary.categories) { category ->
                SindorakuFilterChip(
                    label = category,
                    selected = category == selectedCategory,
                    onClick = { selectedCategory = category }
                )
            }
        }

        SindorakuHairlineDivider()

        if (filteredWords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppTokens.Space.xl)
            ) {
                SindorakuSurfaceCard(backgroundColor = CardTintGray) {
                    SindorakuCaption(text = "Tidak ditemukan")
                    Spacer(Modifier.height(AppTokens.Space.sm))
                    Text(
                        text = "Coba kata kunci lain atau pilih kategori Semua.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = AppTokens.Space.xl,
                    vertical = AppTokens.Space.md
                ),
                verticalArrangement = Arrangement.spacedBy(AppTokens.Space.xxs)
            ) {
                items(filteredWords, key = { it.id }) { item ->
                    val tint = when (item.category) {
                        "Angka" -> CardTintSky
                        "Salam" -> CardTintMint
                        "Keluarga" -> CardTintRose
                        else -> CardTintLavender
                    }
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(AppTokens.Radius.lg))
                            .background(Canvas)
                    ) {
                        SindorakuListRow(
                            title = item.label,
                            subtitle = item.category,
                            badgeTint = tint,
                            onClick = {
                                navController.navigate("word_detail/${item.id}")
                            }
                        )
                        SindorakuHairlineDivider()
                    }
                }
                item { Spacer(Modifier.height(AppTokens.Space.section)) }
            }
        }
    }
}
