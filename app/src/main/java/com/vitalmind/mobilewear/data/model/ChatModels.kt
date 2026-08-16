package com.vitalmind.mobilewear.data.model

data class ChatAnalyzeRequest(
    val analysisDate: String,
    val message: String
)

data class ChatMetadataDto(
    val provider: String?,
    val model: String?,
    val context_used: Boolean?
)

data class ChatAnalyzeDataDto(
    val request_id: String,
    val user_id: String,
    val analysis_date: String,
    val answer: String,
    val metadata: ChatMetadataDto?,
    val disclaimer: String?
)

data class ChatAnalyzeResponse(
    val success: Boolean,
    val data: ChatAnalyzeDataDto?
)