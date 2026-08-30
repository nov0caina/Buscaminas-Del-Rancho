package com.example.data.model

enum class RanchFlagIcon(
    val emoji: String,
    val title: String,
    val description: String,
    val isVip: Boolean = false
) {
    SOMBRERO("🤠", "Sombrero", "Sombrero vaquero tradicional", isVip = false),
    HERRADURA("🐎", "Herradura", "Herradura de caballo fino", isVip = false),
    CACTUS("🌵", "Cactus", "Cactus del desierto", isVip = false),
    CUERNOS("🐂", "Cuernos", "Cuernos bravos de toro", isVip = true),
    DINAMITA("🧨", "Dinamita", "Cartucho de dinamita", isVip = true),
    GALLO("🐓", "Gallo", "Gallo de pelea sinaloense", isVip = true),
    BOTA("👢", "Bota", "Bota vaquera de piel fina", isVip = true),
    GUITARRA("🎸", "Campirano", "Guitarra de música sierreña", isVip = true),
    AGAVE("🌱", "Agave", "Planta tequilera del cerro", isVip = true),
    PISTOLA("🔫", "Pistola", "Gatillo rápido del rancho", isVip = true)
}
