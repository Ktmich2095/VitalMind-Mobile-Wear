package com.vitalmind.mobilewear.data.model

data class SymptomDto(
    val id: Long,
    val heart_rate: Int? = null
)

data class SymptomListResponse(
    val success: Boolean,
    val items: List<SymptomDto>
)

data class HeartRateRequest(
    val heartRate: Int,
    val notes: String? = null
)

data class SymptomMutationResponse(
    val success: Boolean,
    val message: String?,
    val data: SymptomDto?
)