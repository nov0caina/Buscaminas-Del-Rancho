package com.example.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager private constructor(private val appContext: Context) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        private const val PREFS_NAME = "rancho_billing_prefs"
        private const val KEY_PATRON_UNLOCKED = "is_patron_unlocked"

        // Both SKU variations supported to match Google Play Console setup
        val PRODUCT_IDS = listOf("pase-del-patron", "pase_del_patron_unlock")

        @Volatile
        private var INSTANCE: BillingManager? = null

        fun getInstance(context: Context): BillingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _isPatronUnlocked = MutableStateFlow(prefs.getBoolean(KEY_PATRON_UNLOCKED, false))
    val isPatronUnlocked: StateFlow<Boolean> = _isPatronUnlocked.asStateFlow()

    private val _patronPrice = MutableStateFlow("$25.00 MXN")
    val patronPrice: StateFlow<String> = _patronPrice.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var productDetails: ProductDetails? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        startBillingConnection()
    }

    private fun startBillingConnection(onConnected: (() -> Unit)? = null) {
        if (billingClient.isReady) {
            onConnected?.invoke()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "BillingClient conectado con éxito a Google Play")
                    queryProductDetails()
                    queryExistingPurchases()
                    onConnected?.invoke()
                } else {
                    Log.w(TAG, "Fallo al conectar BillingClient: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "BillingService desconectado, reintentando...")
            }
        })
    }

    fun queryProductDetails(onLoaded: (() -> Unit)? = null) {
        if (!billingClient.isReady) {
            startBillingConnection { queryProductDetails(onLoaded) }
            return
        }

        val productList = PRODUCT_IDS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            val productDetailsList = queryProductDetailsResult.productDetailsList
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !productDetailsList.isNullOrEmpty()) {
                val details = productDetailsList.first()
                productDetails = details
                val priceFormatted = details.oneTimePurchaseOfferDetails?.formattedPrice
                if (!priceFormatted.isNullOrBlank()) {
                    _patronPrice.value = priceFormatted
                    Log.d(TAG, "Precio oficial del Pase del Patrón cargado: $priceFormatted")
                }
                onLoaded?.invoke()
            } else {
                Log.d(TAG, "No se encontraron detalles de producto en Play Console aún: ${billingResult.debugMessage}")
            }
        }
    }

    fun queryExistingPurchases(onComplete: ((Boolean) -> Unit)? = null) {
        if (!billingClient.isReady) {
            startBillingConnection { queryExistingPurchases(onComplete) }
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var found = false
                for (purchase in purchasesList) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        for (sku in purchase.products) {
                            if (PRODUCT_IDS.contains(sku)) {
                                found = true
                                handlePurchase(purchase)
                                break
                            }
                        }
                    }
                }
                if (found) {
                    setPatronUnlocked(true)
                }
                onComplete?.invoke(found)
            } else {
                onComplete?.invoke(false)
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity): Boolean {
        if (!billingClient.isReady) {
            startBillingConnection {
                launchPurchaseFlow(activity)
            }
            return false
        }

        val details = productDetails
        if (details == null) {
            queryProductDetails {
                val freshDetails = productDetails
                if (freshDetails != null) {
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(freshDetails)
                            .build()
                    )
                    val flowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()
                    billingClient.launchBillingFlow(activity, flowParams)
                }
            }
            return false
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val response = billingClient.launchBillingFlow(activity, flowParams)
        return response.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Compra cancelada por el usuario")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "El usuario ya posee el Pase del Patrón")
                setPatronUnlocked(true)
            }
            else -> {
                Log.w(TAG, "Error en la compra: ${billingResult.responseCode} - ${billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            setPatronUnlocked(true)

            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                coroutineScope.launch {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "Compra confirmada (acknowledged) con éxito")
                        } else {
                            Log.w(TAG, "Error al confirmar compra: ${billingResult.debugMessage}")
                        }
                    }
                }
            }
        }
    }

    fun setPatronUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean(KEY_PATRON_UNLOCKED, unlocked).apply()
        _isPatronUnlocked.value = unlocked
    }
}
