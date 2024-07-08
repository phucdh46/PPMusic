package com.dhp.musicplayer.core.billing

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

interface BillingClientProvider {
    val billingClient: BillingClient
    suspend fun startBillingConnection(): Result<Unit>
    suspend fun queryPurchases(): Result<List<Purchase>>
    suspend fun queryProductDetails(): Result<List<ProductDetails>>
    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails)
    fun terminateBillingConnection()
}