package com.dhp.musicplayer.core.billing.repository

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.dhp.musicplayer.core.billing.BillingClientProvider
import javax.inject.Inject

class SubscriptionDataRepositoryImpl @Inject constructor(
    private val billingClientProvider: BillingClientProvider
) : SubscriptionDataRepository {

    override suspend fun startBillingConnection() {
        billingClientProvider.startBillingConnection()
    }

    override suspend fun verified(): Boolean {
        val purchases = billingClientProvider.queryPurchases().getOrNull() ?: return false
        val productDetails = billingClientProvider.queryProductDetails().getOrNull() ?: return false
        return purchases.find { purchase ->
            productDetails.any { productDetails ->
                purchase.products.contains(productDetails.productId)
            }
        } != null
    }

    override suspend fun buyPremiumApp(activity: Activity) {
        val productDetails = getProductDetail() ?: return
        billingClientProvider.launchBillingFlow(activity, productDetails)
    }

    override suspend fun getProductDetail(): ProductDetails? {
        return billingClientProvider.queryProductDetails().getOrNull()?.firstOrNull()
    }

    override fun terminateBillingConnection() {
        billingClientProvider.terminateBillingConnection()
    }
}