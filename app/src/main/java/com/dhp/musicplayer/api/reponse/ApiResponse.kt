package com.dhp.musicplayer.api.reponse

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

//fun <T> ApiResponse<T>.isAuthenticationInvalid(): Boolean {
//    return code.equals(invalid_token_code, true)
//}