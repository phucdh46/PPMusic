package com.dhp.musicplayer.core.billing

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.edit
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.dhp.musicplayer.core.common.utils.Logg
import com.dhp.musicplayer.core.datastore.IsEnablePremiumModeKey
import com.dhp.musicplayer.core.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BillingClientProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingClientProvider {
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )
    private var isReady = false

    private val params = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .build()

    override var billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                scope.launch {
                    handlePurchase(purchases)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }
        .enablePendingPurchases(params)
        // Configure other settings.
        .build()

    private suspend fun handlePurchase(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                    val ackPurchaseResult = withContext(Dispatchers.IO) {
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
                    }
                    if (ackPurchaseResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Logg.d("User has acknowledged")
                        scope.launch(Dispatchers.IO) {
                            context.dataStore.edit { preferences ->
                                preferences[IsEnablePremiumModeKey] = true
                            }
                        }
                    } else {
                        Logg.d("User has not acknowledged")
                    }
                }
            }
        }
    }

    override suspend fun startBillingConnection(): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            Logg.d("startBillingConnection")
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Logg.d("onBillingSetupFinished")
                        // The BillingClient is ready. You can query purchases here.
                        isReady = true
                        continuation.resume(Result.success(Unit))
                    } else {
                        Logg.d("onBillingSetupFailed")
                        val retry = retryBillingServiceConnection()
                        isReady = retry
                        if (retry) {
                            Logg.d("onBillingSetupFailed success")
                            continuation.resume(Result.success(Unit))
                        } else {
                            Logg.d("onBillingSetupFailed failure")
                            continuation.resume(Result.failure(Exception("Billing Setup Failed: ${billingResult.debugMessage}")))
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                    Logg.d("onBillingServiceDisconnected")
                    val retry = retryBillingServiceConnection()
                    isReady = retry
                    if (retry) {
                        Logg.d("onBillingServiceDisconnected success")
                        continuation.resume(Result.success(Unit))
                    } else {
                        Logg.d("onBillingServiceDisconnected Exception")
                        continuation.resumeWithException(Exception("Billing Service Disconnected"))
                    }
                }
            })
        }

    private fun retryBillingServiceConnection(): Boolean {
        Logg.d("retryBillingServiceConnection: ready: ${billingClient.isReady}")
        val maxTries = 3
        var tries = 1
        var isConnectionEstablished = false
        do {
            Logg.d("retryBillingServiceConnection:tries $tries")
            try {
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingServiceDisconnected() {
                    }

                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            isConnectionEstablished = true
                            Logg.d("Billing connection retry succeeded.")
                        } else {
                            Logg.d(
                                "Billing connection retry failed: ${billingResult.debugMessage}"
                            )
                        }
                    }
                })
            } catch (e: Exception) {
                e.message?.let { Logg.d(it) }
                tries++
            }
        } while (tries <= maxTries && !isConnectionEstablished)
        return isConnectionEstablished
    }

    override suspend fun queryPurchases(): Result<List<Purchase>> {
        if (!isReady) {
            val result = startBillingConnection()
            Logg.d("queryPurchases: BillingClient is not ready")
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull()!!)
            }
        }
        return suspendCancellableCoroutine { continuation ->
            // Query for existing subscription products that have been purchased.
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { billingResult, purchaseList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(Result.success(purchaseList))
                } else {
                    continuation.resume(Result.failure(Exception("Querying Purchases Failed: ${billingResult.debugMessage}")))
                    Logg.d("queryPurchases: ${billingResult.debugMessage}")
                }
            }
        }
    }

    // Query Google Play Billing for products available to sell and present them in the UI
    override suspend fun queryProductDetails(): Result<List<ProductDetails>> {
        if (!isReady) {
            val result = startBillingConnection()
            Logg.d("queryProductDetails: BillingClient is not ready")
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull()!!)
            }
        }
        return suspendCancellableCoroutine { continuation ->
            val params = QueryProductDetailsParams.newBuilder()
            val productList = mutableListOf<QueryProductDetailsParams.Product>()
            for (product in LIST_OF_PRODUCTS) {
                productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(product)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            }

            params.setProductList(productList)
            val productDetailsResult = runBlocking {
                billingClient.queryProductDetails(params.build())
            }
            Logg.d("queryProductDetails: $productDetailsResult")
            when (productDetailsResult.billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (productDetailsResult.productDetailsList.isNullOrEmpty()) {
                        continuation.resume(Result.failure(Exception("Querying ProductDetails: ${productDetailsResult.billingResult.debugMessage}")))
                    } else {
                        continuation.resume(Result.success(productDetailsResult.productDetailsList!!))
                    }
                }

                else -> {
                    continuation.resume(Result.failure(Exception("Querying ProductDetails: ${productDetailsResult.billingResult.debugMessage}")))
                }
            }
        }
    }

    override fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
//        val selectedOfferToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return
//        val productDetails = productWithProductDetails.value[PREMIUM_APP] ?: return
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(productDetails)
                // For One-time product, "setOfferToken" method shouldn't be called.
                // For subscriptions, to get an offer token, call ProductDetails.subscriptionOfferDetails()
                // for a list of offers that are available to the user
//                .setOfferToken(selectedOfferToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        if (!billingClient.isReady) {
            Logg.d("launchBillingFlow: BillingClient is not ready")
        }
        billingClient.launchBillingFlow(activity, billingFlowParams)

    }

    // End Billing connection.
    override fun terminateBillingConnection() {
        Logg.d("Terminating connection")
        billingClient.endConnection()
    }

    companion object {
        const val PREMIUM_APP = "premium_app"
        const val PREMIUM_SUB = "premium_version"
        val LIST_OF_PRODUCTS = listOf(PREMIUM_APP)
    }
}