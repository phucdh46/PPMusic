package com.dhp.musicplayer.core.billing.repository

import android.app.Activity
import com.android.billingclient.api.ProductDetails

interface SubscriptionDataRepository {
    suspend fun startBillingConnection()
    suspend fun verified(): Boolean
    suspend fun buyPremiumApp(activity: Activity)
    suspend fun getProductDetail(): ProductDetails?
    fun terminateBillingConnection()
}