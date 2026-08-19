package com.example.data.model

data class RevealCluster(
    val id: Long,
    val originRow: Int,
    val originCol: Int,
    val minRow: Int,
    val maxRow: Int,
    val minCol: Int,
    val maxCol: Int,
    val centerRow: Float,
    val centerCol: Float,
    val cellCount: Int
)
