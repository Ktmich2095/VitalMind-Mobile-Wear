package com.vitalmind.mobilewear.data.api

import com.vitalmind.mobilewear.data.model.HeartRateRequest
import com.vitalmind.mobilewear.data.model.SymptomListResponse
import com.vitalmind.mobilewear.data.model.SymptomMutationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SymptomApiService {

    @GET("symptoms")
    suspend fun getSymptoms(
        @Query("page")
        page: Int = 1,

        @Query("pageSize")
        pageSize: Int = 1
    ): SymptomListResponse

    @POST("symptoms")
    suspend fun createSymptom(
        @Body
        request: HeartRateRequest
    ): SymptomMutationResponse

    @PUT("symptoms/{id}")
    suspend fun updateSymptom(
        @Path("id")
        id: Long,

        @Body
        request: HeartRateRequest
    ): SymptomMutationResponse
}