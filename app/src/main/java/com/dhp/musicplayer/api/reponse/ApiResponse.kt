package com.dhp.musicplayer.api.reponse

import com.google.gson.annotations.SerializedName

//
data class ApiResponse<T>(
    @SerializedName("code")
    val code: String?,
    @SerializedName("message")
    var message: String?,
    @SerializedName("result")
    val result: T?
)

fun <T> ApiResponse<T>.isSuccess(): Boolean {
    return code.equals("ok", true)
}

//fun <T> ApiResponse<T>.isAuthenticationInvalid(): Boolean {
//    return code.equals(invalid_token_code, true)
//}