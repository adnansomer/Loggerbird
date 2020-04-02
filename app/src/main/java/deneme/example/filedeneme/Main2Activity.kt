package deneme.example.filedeneme

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.view.drawToBitmap
import com.android.billingclient.api.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import loggerbird.LoggerBird
import com.google.gson.GsonBuilder
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_main2.*
import org.json.JSONObject


class Main2Activity : AppCompatActivity(), PurchasesUpdatedListener {

    private var billingResponseCode: Int = 0
    private lateinit var billingClient: BillingClient
    private var skuList: MutableList<String> = ArrayList()
    private lateinit var skuDetails: SkuDetails
    private lateinit var acknowledgePurchaseParams: AcknowledgePurchaseParams
    private lateinit var billingFlowParams: BillingFlowParams
    private lateinit var skuDetailsParams: SkuDetailsParams
    private var jsonObject: JSONObject = JSONObject()
    private lateinit var imageViewTempBlur: ImageView
    private lateinit var imageViewTemp: ImageView
    private lateinit var imageViewCombined: ImageView
    private var handler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        Log.d("super_class", Main2Activity::class.java.superclass!!.simpleName)
        setupBillingClient()
        blurImage()
        button_dummy.setOnClickListener(View.OnClickListener {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.main_activity_2,
                    FragmentMain3.newInstance(), "FragmentMain3")
                .commit()
        })
    }

    override fun onStart() {
        super.onStart()
        Log.d("life_cycle_state_start", this.lifecycle.currentState.name)
    }

    private fun setupBillingClient() {
        billingClient = BillingClient
            .newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.d("bill", "Disconnected")
            }

            override fun onBillingSetupFinished(p0: BillingResult?) {
                Log.d("bill", "finished")
                if (p0?.responseCode == 0) {
                    Log.d("bill", "result_ok")
                    skuList.add("dummyproduct1")
                    skuList.add("dummyproduct2")
                    skuList.add("dummyproduct3")
                    skuList.add("dummyproduct4")
                    skuList.add("dummyproduct5")
                    skuList.add("dummyproduct6")
                    skuList.add("dummyproduct7")
                    skuList.add("dummyproduct8")
                    skuList.add("dummyproduct9")
                    skuList.add("dummyproduct10")
                    skuList.add("dummyproduct11")
                    skuList.add("dummyproduct12")
                    skuList.add("dummyproduct13")
                    skuList.add("dummyproduct14")
                    onLoadProductsClicked()

                } else {
                    Log.d("bill", "result_finished")
                }
            }
        })
    }

    fun onLoadProductsClicked() {
        convertJson()
        if (billingClient.isReady) {
            skuDetailsParams = SkuDetailsParams
                .newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)
                .build()
            billingClient.querySkuDetailsAsync(skuDetailsParams) { responseCode, skuDetailsList ->
                if (responseCode.responseCode == 0) {
                    // LogDeneme.saveInAPurchaseDetails(skuDetails=skuDetailsParams)
                    initProduct(skuDetailsList)
                    Log.d("querySkuDetailsAsync", "querySkuDetailsAsync Successfull")

                }
//
//                    Log.d("bill_onload_products", responseCode.responseCode.toString())
//                    Log.d("bill_client",billingClient.isReady.toString()+","+billingClient.toString())
//                    initProduct(skuDetailsList)
                else {
                    Log.d("bill_onload_failure", responseCode.responseCode.toString())
                }
            }
        } else {
            println("Billing Client not ready")
        }
    }

    private fun initProduct(skuDetailList: List<SkuDetails>) {

        try {
            billingFlowParams = BillingFlowParams
                .newBuilder()
                // .setSkuDetails(skuDetailList[2])
                .setSkuDetails(skuDetailList[0])//skuDetailList:List<SkuDetails>
                .build()
            billingClient.launchBillingFlow(this, billingFlowParams)
            Log.d("launch_billing_flow", billingResponseCode.toString())
            Log.d("launch_billing_flow", billingFlowParams.sku)
            Log.d("launching_billing_flow", "billing flow success!")

            Log.d(
                "sku_details",
                billingFlowParams.sku + "," + billingFlowParams.skuDetails + "," + billingFlowParams.skuType + "," + billingClient.isReady
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPurchasesUpdated(p0: BillingResult?, p1: MutableList<Purchase>?) {

        if (p0?.responseCode == 0 && p1 != null) {
            acknowledgePurchase(p1[0].purchaseToken, p1[0].developerPayload)
        } else if (p0?.responseCode == 1) {
            Log.d("bill_on_purchase_update", p0.responseCode.toString() + "," + p0.debugMessage)
        } else {
            Log.d("bill_on_purchase_update", p0?.responseCode.toString() + "," + p0?.debugMessage)
        }
        // LogDeneme.saveInAPurchaseDetails(billingClient = billingClient,skuDetails = skuDetailsParams,billingResult =p0)
        Log.d("bill_on_purchase", p0?.responseCode.toString())
    }

    private fun acknowledgePurchase(purchaseToken: String, payload: String?) {
        acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .setDeveloperPayload(payload)
            .build()
        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
//            LoggerBird.takeInAPurchaseDetails(billingResult=billingResult,acknowledgePurchaseParams = acknowledgePurchaseParams,billingClient = billingClient,skuDetailsParams= skuDetailsParams,billingFlowParams = billingFlowParams)
            // LoggerBird.saveInAPurchaseDetails()
            Log.d(
                "bill_on_acknowledge",
                responseCode.toString() + "," + acknowledgePurchaseParams.purchaseToken
            )
        }
    }

    private fun convertJson() {
        val gson = GsonBuilder().create()
        var skulDummy: String = gson.toJson(skuList)
        var intCounter: Int = 0
        do {

            jsonObject.put(intCounter.toString(), skuList.get(intCounter))
            intCounter++
            if (skuList.size == intCounter) {
                break
            }
        } while (skuList.iterator().hasNext())
    }

    private fun blurImage() {
        handler.post {
//            imageViewTemp = ImageView(this)
//            imageViewTemp = imageView
//            imageViewTemp.setImageBitmap(takeScreenShot(view = imageViewTemp))

            imageViewTempBlur = ImageView(this)
            imageViewTempBlur = imageView
            imageViewTempBlur.setImageBitmap(takeScreenShotWithBlur(view = imageViewTempBlur))

//
//            imageViewCombined = ImageView(this)
//            imageViewCombined.setImageBitmap(combineImages(imageViewTemp, imageViewTempBlur))
//            linear_cici.background = imageViewCombined.drawable
        }


    }

    private fun takeScreenShotWithBlur(view: View): Bitmap {
//        val viewTemp: View = (view.parent as View)
        val bitmap: Bitmap = Bitmap.createBitmap(
            (linear_cici.width - imageViewTempBlur.width),
            (linear_cici.height - imageViewTempBlur.height),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        Glide.with(this)
            .load(imageViewTempBlur.drawToBitmap())
            .apply(bitmapTransform(BlurTransformation()))
            .apply(bitmapTransform(BlurTransformation()))
            .into(imageViewTempBlur)
        return bitmap
    }

    private fun takeScreenShot(view: View): Bitmap {
        val bitmap: Bitmap = Bitmap.createBitmap(
            (imageViewTemp.width),
            (imageViewTemp.height),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun combineImages(view: View, viewBlur: View): Bitmap {
        val bitmap: Bitmap = Bitmap.createBitmap(
            (linear_cici.width),
            (linear_cici.height),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawBitmap((imageViewTemp.drawable as BitmapDrawable).bitmap,0f,0f,null)
        canvas.drawBitmap((imageViewTempBlur.drawable as BitmapDrawable).bitmap,bitmap.width.toFloat(),bitmap.height.toFloat(),null)
        return bitmap

    }
}
