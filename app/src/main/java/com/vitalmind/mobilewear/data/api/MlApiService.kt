package com.vitalmind.mobilewear.data.api

import com.vitalmind.mobilewear.data.model.MlAnalyzeRequest
import com.vitalmind.mobilewear.data.model.MlAnalyzeResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface MlApiService {

    @POST("ml/analyze")
    suspend fun analyze(
        @Body request: MlAnalyzeRequest
    ): MlAnalyzeResponse
}