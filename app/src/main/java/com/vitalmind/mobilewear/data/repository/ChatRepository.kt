package com.vitalmind.mobilewear.data.repository

import com.vitalmind.mobilewear.data.api.ChatApiService
import com.vitalmind.mobilewear.data.model.ChatAnalyzeRequest
import com.vitalmind.mobilewear.data.model.ChatAnalyzeResponse

class ChatRepository(
    private val api: ChatApiService
) {

    suspend fun ask(
        analysisDate: String,
        message: String
    ): ChatAnalyzeResponse {

        return api.analyzeChat(
            ChatAnalyzeRequest(
                analysisDate = analysisDate,
                message = message
            )
        )
    }
}