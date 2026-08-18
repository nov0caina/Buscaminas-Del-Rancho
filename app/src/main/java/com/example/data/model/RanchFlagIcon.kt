package com.example.data.model

enum class RanchFlagIcon(
    val emoji: String,
    val title: String,
    val description: String
) {
    SOMBRERO("🤠", "Sombrero", "Sombrero vaquero tradicional"),
    CUERNOS("🐂", "Cuernos", "Cuernos bravos de toro"),
    HERRADURA("🐎", "Herradura", "Herradura de caballo fino"),
    CACTUS("🌵", "Cactus", "Cactus del desierto"),
    DINAMITA("🧨", "Dinamita", "Cartucho de dinamita"),
    GALLO("🐓", "Gallo", "Gallo de pelea sinaloense"),
    BOTA("👢", "Bota", "Bota vaquera de piel fina"),
    GUITARRA("🎸", "Campirano", "Guitarra de música sierreña"),
    AGAVE("🌱", "Agave", "Planta tequilera del cerro"),
    PISTOLA("🔫", "Pistola", "Gatillo rápido del rancho")
}
