package com.dpi.changer.data.model

import java.util.UUID

data class Preset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dpi: Int,
    val timestamp: Long = System.currentTimeMillis()
)