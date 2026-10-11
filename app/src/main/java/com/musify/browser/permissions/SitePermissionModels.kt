package com.musify.browser.permissions

enum class SitePermissionType(val label: String, val icon: String) {
    GEOLOCATION("Location", "📍"),
    CAMERA("Camera", "📷"),
    MICROPHONE("Microphone", "🎙"),
    NOTIFICATIONS("Notifications", "🔔")
}

enum class SitePermissionDecision {
    PROMPT,
    GRANTED,
    DENIED
}

data class SitePermission(
    val origin: String,
    val type: SitePermissionType,
    val decision: SitePermissionDecision,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

data class SitePermissionPromptRequest(
    val id: String,
    val origin: String,
    val type: SitePermissionType
)
