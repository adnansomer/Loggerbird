package deneme.example.filedeneme

import com.android.billingclient.api.*

class BillingRepository :
    PurchasesUpdatedListener, BillingClientStateListener,
    ConsumeResponseListener, SkuDetailsResponseListener {



    override fun onPurchasesUpdated(p0: BillingResult?, p1: MutableList<Purchase>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBillingServiceDisconnected() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBillingSetupFinished(p0: BillingResult?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConsumeResponse(p0: BillingResult?, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSkuDetailsResponse(p0: BillingResult?, p1: MutableList<SkuDetails>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}