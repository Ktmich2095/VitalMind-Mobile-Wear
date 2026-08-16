package com.vitalmind.mobilewear.data.model

data class MlAnalyzeRequest(
    val analysisDate: String
)

data class RiskClassificationDto(
    val risk_level: String,
    val confidence: Double,
    val probabilities: Map<String, Double>
)

data class WellbeingDto(
    val score: Double,
    val level: String
)

data class MlResultsDto(
    val risk_classification: RiskClassificationDto,
    val wellbeing: WellbeingDto,
    val calculated_bmi: Double?,
    val recommendations: List<String>
)

data class ModelVersionsDto(
    val risk_classifier: String,
    val wellbeing_regressor: String
)

data class MissingDataReportDto(
    val required_missing: List<String>,
    val imputed_fields: List<String>,
    val warnings: List<String>
)

data class MlAnalyzeDataDto(
    val request_id: String,
    val user_id: String,
    val analysis_date: String,
    val results: MlResultsDto,
    val model_versions: ModelVersionsDto,
    val missing_data_report: MissingDataReportDto,
    val disclaimer: String
)

data class MlAnalyzeResponse(
    val success: Boolean,
    val data: MlAnalyzeDataDto?
)