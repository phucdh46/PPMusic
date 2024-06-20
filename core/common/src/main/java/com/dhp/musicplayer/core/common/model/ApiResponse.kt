package com.dhp.musicplayer.core.common.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: String?,
    var message: String?,
    val result: T?
)

fun <T> ApiResponse<T>.isSuccess(): Boolean {
    return code.equals("ok", true)
}

fun <T, R> convertApiResponse(
    apiResponse: ApiResponse<T>,
    transform: (T?) -> R?
): ApiResponse<R> {
    return ApiResponse(
        code = apiResponse.code,
        message = apiResponse.message,
        result = transform(apiResponse.result)
    )
}