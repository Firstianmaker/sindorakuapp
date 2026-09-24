package com.example.sindoraku.ui.vocabulary

/**
 * Kamus BISINDO yang ditampilkan di UI (15 kata).
 * Satu sumber data untuk daftar kata & halaman detail.
 */
data class BisindoWord(
    val id: String,
    val label: String,
    val category: String,
    val meaning: String
)

object BisindoVocabulary {

    const val WORD_COUNT = 15

    val words: List<BisindoWord> = listOf(
        BisindoWord("ada", "Ada", "Umum", "Menunjukkan keberadaan atau ketersediaan sesuatu."),
        BisindoWord("bantuan", "Bantuan", "Permintaan", "Menyatakan kebutuhan akan pertolongan dari orang lain."),
        BisindoWord("boleh", "Boleh", "Permintaan", "Menanyakan atau memberi izin untuk melakukan sesuatu."),
        BisindoWord("butuh", "Butuh", "Permintaan", "Menyatakan bahwa sesuatu diperlukan atau dibutuhkan."),
        BisindoWord("kamu", "Kamu", "Orang", "Merujuk pada lawan bicara atau orang yang diajak berbicara."),
        BisindoWord("kapan", "Kapan", "Umum", "Menanyakan waktu atau saat terjadinya suatu kejadian."),
        BisindoWord("maaf", "Maaf", "Ekspresi", "Ungkapan permintaan maaf atau penyesalan."),
        BisindoWord("makan", "Makan", "Aktivitas", "Menunjukkan aktivitas mengonsumsi makanan."),
        BisindoWord("masalah", "Masalah", "Umum", "Menyatakan adanya kesulitan atau hal yang perlu diselesaikan."),
        BisindoWord("mau", "Mau", "Aktivitas", "Menunjukkan keinginan atau niat untuk melakukan sesuatu."),
        BisindoWord("pergi", "Pergi", "Aktivitas", "Menunjukkan pergerakan menjauh dari suatu tempat."),
        BisindoWord("saya", "Saya", "Orang", "Merujuk pada diri sendiri sebagai pembicara."),
        BisindoWord("siapa", "Siapa", "Orang", "Menanyakan identitas atau orang yang dimaksud."),
        BisindoWord("terimakasih", "Terimakasih", "Ekspresi", "Ungkapan rasa terima kasih atau penghargaan."),
        BisindoWord("tolong", "Tolong", "Permintaan", "Digunakan saat meminta bantuan secara sopan.")
    )

    val categories: List<String> = listOf(
        "Semua",
        "Orang",
        "Aktivitas",
        "Ekspresi",
        "Permintaan",
        "Umum"
    )

    fun findByRouteArg(arg: String?): BisindoWord? {
        if (arg.isNullOrBlank()) return null
        val key = arg.trim().lowercase()
        return words.find { it.id == key || it.label.equals(arg, ignoreCase = true) }
    }

    fun relatedWords(word: BisindoWord, limit: Int = 4): List<BisindoWord> =
        words
            .filter { it.id != word.id && it.category == word.category }
            .take(limit)

    fun filter(categoryLabel: String, query: String): List<BisindoWord> =
        words.filter { item ->
            (categoryLabel == "Semua" || item.category == categoryLabel) &&
                (query.isBlank() ||
                    item.label.contains(query, ignoreCase = true) ||
                    item.id.contains(query, ignoreCase = true))
        }
}
