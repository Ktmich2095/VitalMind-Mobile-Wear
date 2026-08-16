package com.vitalmind.mobilewear.data.repository

import com.vitalmind.mobilewear.data.api.MlApiService
import com.vitalmind.mobilewear.data.model.MlAnalyzeRequest
import com.vitalmind.mobilewear.data.model.MlAnalyzeResponse

class MlRepository(
    private val api: MlApiService
) {

    suspend fun analyze(
        analysisDate: String
    ): MlAnalyzeResponse {

        return api.analyze(
            MlAnalyzeRequest(
                analysisDate = analysisDate
            )
        )
    }
}