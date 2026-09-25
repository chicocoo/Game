package com.atoll.app.services

import android.app.Activity
import android.content.Context
import android.util.Log
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
import com.atoll.app.R

/** Achat unique « Sans pub » via Google Play Billing. */
class BillingService(context: Context, private val onOwned: (Boolean) -> Unit) {
    private val tag = "Billing"
    private val productId = context.getString(R.string.product_remove_ads)
    private var details: ProductDetails? = null
    var price: String? = null
        private set

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener { result, purchases -> if (result.responseCode == BillingClient.BillingResponseCode.OK) handle(purchases.orEmpty()) }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    fun connect() {
        if (client.isReady) { refresh(); return }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) refresh()
                else Log.w(tag, "setup: ${result.debugMessage}")
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun refresh() {
        val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(BillingClient.ProductType.INAPP).build(),
        )).build()
        client.queryProductDetailsAsync(params) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                details = list.productDetailsList.firstOrNull()
                price = details?.oneTimePurchaseOfferDetails?.formattedPrice
            }
        }
        restore()
    }

    fun restore() {
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) handle(purchases)
        }
    }

    fun buy(activity: Activity): Boolean {
        val d = details ?: run { connect(); return false }
        val flow = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(d).build()))
            .build()
        return client.launchBillingFlow(activity, flow).responseCode == BillingClient.BillingResponseCode.OK
    }

    private fun handle(purchases: List<Purchase>) {
        val owned = purchases.filter { productId in it.products && it.purchaseState == Purchase.PurchaseState.PURCHASED }
        owned.filter { !it.isAcknowledged }.forEach { p ->
            client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()) {}
        }
        if (owned.isNotEmpty()) onOwned(true)
    }
}
