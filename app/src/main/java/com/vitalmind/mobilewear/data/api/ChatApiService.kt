package com.vitalmind.mobilewear.data.api

import com.vitalmind.mobilewear.data.model.ChatAnalyzeRequest
import com.vitalmind.mobilewear.data.model.ChatAnalyzeResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {

    @POST("ml/chat/analyze")
    suspend fun analyzeChat(
        @Body request: ChatAnalyzeRequest
    ): ChatAnalyzeResponse
}