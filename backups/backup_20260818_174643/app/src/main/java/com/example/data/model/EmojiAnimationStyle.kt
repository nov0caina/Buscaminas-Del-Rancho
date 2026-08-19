package com.example.data.model

enum class EmojiAnimationStyle(
    val title: String,
    val description: String,
    val previewBadge: String
) {
    ESTAMPA_RANCHERA("Sello de Rancho", "Impacto firme con rebote elástico", "🎯"),
    POLVAREDA_GIRO("Polvareda y Giro", "Emerge girando entre ráfagas de polvo", "🌪️"),
    LAZO_NORTEÑO("Lazo Vaquero", "Giro rápido de 360° con lazada", "🤠"),
    BRINCO_CABALLO("Brinco Norteño", "Doble salto con resorte dinámico", "🐎"),
    PULSO_DINAMITA("Pulso Explosivo", "Contracción y expansión continua", "⚡")
}
