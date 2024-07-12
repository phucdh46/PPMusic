package com.dhp.musicplayer.core.data.firebase

interface FirebaseService {
    suspend fun fetchConfiguration(): Boolean
    fun getRewardedAdUnitId(): String
}