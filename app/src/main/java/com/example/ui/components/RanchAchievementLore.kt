package com.example.ui.components

data class AchievementLore(
    val id: String,
    val title: String,
    val badgeName: String,
    val sinaloaPhrase: String,
    val soundActionLabel: String,
    val soundTheme: String
)

object RanchAchievementLore {
    private val loreMap = mapOf(
        "first_win" to AchievementLore(
            id = "first_win",
            title = "Primer Espuelazo 🤠",
            badgeName = "Vaquero Graduado",
            sinaloaPhrase = "¡Eso es todo, plebe! ¡Ya cayó la primera y puro pa' delante!",
            soundActionLabel = "¡Echar Grito Vaquero! 🤠",
            soundTheme = "first_win"
        ),
        "patron_experto" to AchievementLore(
            id = "patron_experto",
            title = "El Patrón del Rancho 👑",
            badgeName = "Patrón Supremo",
            sinaloaPhrase = "¡Aquí nomás mis chicharrones truenan, compa!",
            soundActionLabel = "¡Tocar Tambora de Mando! 🎺",
            soundTheme = "patron_experto"
        ),
        "fast_hand" to AchievementLore(
            id = "fast_hand",
            title = "Rápido como el Viento ⚡",
            badgeName = "Velocidad Costera",
            sinaloaPhrase = "¡A doscientos por la costera! ¡Ni el polvo vieron!",
            soundActionLabel = "¡Acelerar la Troca! ⚡",
            soundTheme = "fast_hand"
        ),
        "cazador_iguanas" to AchievementLore(
            id = "cazador_iguanas",
            title = "Cazador de Iguanas 🦎",
            badgeName = "Tirador del Guamúchil",
            sinaloaPhrase = "¡No se me va ni una viva del guamúchil, compa!",
            soundActionLabel = "¡Tirar Resorterazo! 🎯",
            soundTheme = "cazador_iguanas"
        ),
        "minero_veterano" to AchievementLore(
            id = "minero_veterano",
            title = "Minero Sinaloense ⛏️",
            badgeName = "Barretero de Ley",
            sinaloaPhrase = "¡Puro jale macizo, pariente! ¡Aquí no nos rajamos!",
            soundActionLabel = "¡Picar Veta de Oro! 💎",
            soundTheme = "minero_veterano"
        ),
        "sin_banderas" to AchievementLore(
            id = "sin_banderas",
            title = "A Ojo de Buen Cubero 👁️",
            badgeName = "Colmillo Fino",
            sinaloaPhrase = "¡A puro colmillo, pariente! ¿Pa' qué queremos banderitas?",
            soundActionLabel = "¡Activar Colmillo! 👁️",
            soundTheme = "sin_banderas"
        )
    )

    fun getLore(achievementId: String): AchievementLore {
        return loreMap[achievementId] ?: AchievementLore(
            id = achievementId,
            title = "Logro del Rancho 🏆",
            badgeName = "Hazaña Cumplida",
            sinaloaPhrase = "¡Puro pa' delante, pariente!",
            soundActionLabel = "¡Festejar Logro! 🤠",
            soundTheme = "default"
        )
    }
}

