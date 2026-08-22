package com.example.milibrary

import com.google.gson.annotations.SerializedName

// Modelo para health check - CORREGIDO
data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("database_status") val databaseStatus: String? // Cambié el nombre del campo
) {

}
