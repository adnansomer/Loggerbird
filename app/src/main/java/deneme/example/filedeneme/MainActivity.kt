package deneme.example.filedeneme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import androidx.appcompat.app.AlertDialog
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ComponentActivity
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import deneme.example.filedeneme.ApiServiceInterface.Companion.client
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort
import kotlinx.coroutines.*
import loggerbird.LoggerBird
import io.reactivex.disposables.Disposable
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.recycler_view_item.*
import loggerbird.LoggerBird.Companion.loggerBirdInterceptorClient
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import okhttp3.Request
import retrofit2.Callback
import retrofit2.Retrofit
import org.json.JSONObject;
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.NullPointerException
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory


class MainActivity : AppCompatActivity() {
    private var logs: String = ""
    private var follows: String = "0"
    private var followers: String = "0"
    private var controlEmpty: Boolean = false
    private var controlPermission: Boolean = false
    private val WRITE_STORAGE_REQUEST_CODE: Int = 101
    private val Read_STORAGE_REQUEST_CODE: Int = 102
    private val FILE_TEXT_REQUEST_CODE: Int = 103
    private val INTERNET_REQUEST_CODE = 104
    private val NETWORK_REQUEST_CODE = 105
    private var getFilePath: String = ""
    private var counterlist = arrayListOf<RealmItem>()
    private val coroutineCallInternet = CoroutineScope(Dispatchers.IO)
    private val coroutineCall = CoroutineScope(Dispatchers.IO)
    private lateinit var realmInstance: Realm
    private lateinit var realmInstanceInsert: Realm
    private lateinit var realmLooper: Looper
    var stringBuilderComponent: java.lang.StringBuilder = java.lang.StringBuilder()
    lateinit var fileDirectoryException: File
    lateinit var fileDirectoryRetrofit: File
    private lateinit var jsonObject: JSONObject
    private val transformerFactory: TransformerFactory = TransformerFactory.newInstance()
    private val transformer: Transformer = transformerFactory.newTransformer()
    private var recyclerViewList: ArrayList<RecyclerModel> = ArrayList()
    var disposable: Disposable? = null
    private lateinit var adapter: RecyclerViewAdapter
    private var coroutineCallComponent = CoroutineScope(Dispatchers.IO)

    companion object {
        var BaseUrl = "http://api.openweathermap.org/"
        var AppId = "2e65127e909e178d0af311a81f39948c"
        var lat = "35"
        var lon = "139"
    }

    init {
//        LoggerBird.logAttachLifeCycleObservers(context = this)
    }

    fun getCurrentData() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherService::class.java)
        val call = service.getCurrentWeatherData(lat, lon, AppId)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.code() == 200) {

                    val weatherResponse = response.body()!!
                    Log.d("weather_response", response.body().toString())
                    val stringBuilder = "Country: " +
                            weatherResponse.sys!!.country +
                            "\n" +
                            "Temperature: " +
                            weatherResponse.main!!.temp +
                            "\n" +
                            "Temperature(Min): " +
                            weatherResponse.main!!.temp_min +
                            "\n" +
                            "Temperature(Max): " +
                            weatherResponse.main!!.temp_max +
                            "\n" +
                            "Humidity: " +
                            weatherResponse.main!!.humidity +
                            "\n" +
                            "Pressure: " +
                            weatherResponse.main!!.pressure
//                    val httpUrl: HttpUrl = HttpUrl.Builder()
//                        .scheme("http")
//                        .host("api.openweathermap.org")
//                        .addPathSegment("data")
//                        .addPathSegment("2.5")
//                        .addPathSegment("weather")
//                        .addQueryParameter("lat","35")
//                        .addQueryParameter("lon","139")
//                        .addQueryParameter("APPID","2e65127e909e178d0af311a81f39948c")
//                        .addPathSegment("search")
//                        .addQueryParameter("q", "DNA")
//                        .addQueryParameter("q", "DNA2")
//                        .addQueryParameter("q", "DNA3")
//                        .addQueryParameter("z", "title:RNA")
//                        .build();
//
//                    val fromBodyBuilder = FormBody.Builder()
//                    val request = Request.Builder()
//                        .url("http://api.openweathermap.org/data/2.5/weather?&lat=35&lon=139&APPID=2e65127e909e178d0af311a81f39948c")
//                        .post(fromBodyBuilder.build())
//                        .build()
                    coroutineCallInternet.async {
                        val getUrl: URL =
                            URL("http://api.openweathermap.org/data/2.5/weather?&lat=35&lon=139&APPID=2e65127e909e178d0af311a81f39948c ")
                        val internetConnection: HttpURLConnection =
                            getUrl.openConnection() as HttpURLConnection
//                        val responseDummy = client.newCall(request).execute()
                        LoggerBird.callOkHttpRequestDetails(
                            url = "https://api.openweathermap.org/data/2.5/weather?&lat=35&lon=139&APPID=2e65127e909e178d0af311a81f39948c",
                            okHttpURLConnection = internetConnection
                        )
                    }
                    //weatherData!!.text = stringBuilder
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                //weatherData!!.text = t.message
            }
        })
    }

//    val TAG_ACTIVITY_NAME:String="MainActivity"
//    val TAG_ONCREATE:String="Activity In OnCreate State"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("life_cycle_state_create", this.lifecycle.currentState.name)
//        LoggerBird.logInit(context = this)
//        progressBar=findViewById(R.id.progressBar)
        val intent: Intent = getIntent()
        val uri: Uri? = intent.data
        Log.d("deep_link_url", uri.toString())
        //addRecyclerViewList()
//        recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//        adapter = RecyclerViewAdapter(this,recyclerViewList)
//        recycler_view.adapter = adapter
//        LoggerBird.registerRecyclerViewObservers(recycler_view)

//        (this as androidx.activity.ComponentActivity).prepareCall(
//            OnActivityResultContract(),
//            OnActivityResultListener()
//        )


//        LogDeneme.logLifeCycleDetails()
//        LogDeneme.logAttach()
//        permissions()
//        if (permissions()) {
//            implementRealm()
//
//        }
        button_add.setOnClickListener() {
            val coroutineScope = CoroutineScope(Dispatchers.IO)
                throw  NullPointerException("Parameter Type cannot be null");


            //recyclerViewList.removeAt(0)
            //recyclerViewList.add(RecyclerModel("hello how are you"))
//            adapter.notifyDataSetChanged()
//            LoggerBird.callComponentDetails(
//                view = recycler_view,
//                resources = recycler_view.resources
//            )
//            LoggerBird.callComponentDetails(view = button_add,resources = button_add.resources)
//            LoggerBird.callEmailSender(context = this)
//            for (x in 1..5) {
//                LoggerBird.callComponentDetails(view = button_add, resources = button_add.resources)
//                LoggerBird.callLifeCycleDetails()
//                LoggerBird.callCpuDetails()

//            }
//
//            try {
//
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }


//            LoggerBird.callEnqueue()


// )
//            LogDeneme.saveComponentDetails()
            //LoggerBird.saveComponentDetails()
//            val emailFile:File=File(this.filesDir,"component_details.txt")

//            LogDeneme.sendLogDetailsAsEmail(file=emailFile,context = this,rootView = rootView )


            //            LogDeneme.saveAllDetails(
//                fileName = "berk_deneme",
//                context = this,
//                view = button_add,
//                resources = resources
//            )
//            val stringWriter:StringWriter=StringWriter()
//            val filePathTemp:String = File(this.filesDir,"activity_main.xml").path
//            val documentBuilderFactory:DocumentBuilderFactory=DocumentBuilderFactory.newInstance()
//            val documentBuilder:DocumentBuilder=documentBuilderFactory.newDocumentBuilder()
//            val document: Document=documentBuilder.parse(File(filePathTemp))
//            transformer.transform(DOMSource(document), StreamResult(stringWriter))
//            Log.d("xml_layout",stringWriter.getBuffer().toString())
//
//
//            val filePath = this.getFilesDir()
//            fileDirectoryRetrofit = File(filePath, "example")

//            saveComponentDetails(fileDirectory,button_add,resources,this)

//            Toast.makeText(this,"Application Performance:"+ActivityManager.getMyMemoryState(localMemoryInfo))
//            if (permissions() && checkEmpty()) {
//                coroutineCallInternet.async { httpRequest("http://example.com") }
//
////                AsyncHttpRequst("http://example.com").execute()
//            }
        }
        button_read_logs.setOnClickListener(View.OnClickListener {
            throw  NullPointerException("asdf");
//            getCurrentData()
//            beginSearch("dog", this)
            // LogDeneme.saveComponentDetails(view=button_read_logs,resources = button_read_logs.resources)
            //            LogDeneme.saveComponentDetails(null,button_read_logs,button_read_logs.resources,this)
//            LogDeneme.saveComponentDetails(null,null,null,this)
//            writeTextFile()
        })

        button_next_activity.setOnClickListener({
            //            LogDeneme.saveComponentDetails(context = this,view = button_next_activity,resources = button_next_activity.resources)
//            LogDeneme.saveAllDetails(context=this)
            //LogDeneme.saveComponentDetails(view=button_next_activity,resources = button_next_activity.resources)
            // LoggerBird.saveLifeCycleDetails()

//            LoggerBird.takeLifeCycleDetails()


            startActivity(Intent(this@MainActivity, Main2Activity::class.java))
        })

        button_performance.setOnClickListener {
            LoggerBird.callLifeCycleDetails()

            //            LoggerBird.takeDeviceInformationDetails()
//            LoggerBird.takeDevicePerformanceDetails()
//            LoggerBird.takeDeviceCpuDetails()


        }
    }

//    private fun addRecyclerViewList() {
//        recyclerViewList.add(RecyclerModel("berk"))
//        recyclerViewList.add(RecyclerModel("berk1"))
//        recyclerViewList.add(RecyclerModel("berk2"))
//        recyclerViewList.add(RecyclerModel("berk3"))
//        recyclerViewList.add(RecyclerModel("berk4"))
//        recyclerViewList.add(RecyclerModel("berk5"))
//        recyclerViewList.add(RecyclerModel("berk6"))
//
//    }

    private fun beginSearch(srsearch: String, context: Context) {
        var retrofit: Retrofit? = ApiServiceInterface.createObject()
        val ApiService by lazy {
            ApiServiceInterface.create(this)
//
        }
        ApiService.run {
            hitCountCheck().enqueue(object :
                Callback<RetroFitModel.Result> {
                override fun onFailure(call: Call<RetroFitModel.Result>, t: Throwable) {
                    t.printStackTrace()
                }

                override fun onResponse(
                    call: Call<RetroFitModel.Result>,
                    response: Response<RetroFitModel.Result>?
                ) {
                    Log.d("response", "response Success!")

//                    val httpUrl: HttpUrl = HttpUrl.Builder()
//                        .scheme("http")
//                        .host("api.openweathermap.org")
////                        .addPathSegment("search")
////                        .addQueryParameter("q", "DNA")
////                        .addQueryParameter("q", "DNA2")
////                        .addQueryParameter("q", "DNA3")
////                        .addQueryParameter("z", "title:RNA")
//                        .build();
//
//                    val fromBodyBuilder = FormBody.Builder()
//                    val request = Request.Builder()
//                        .url(httpUrl)
//                        .post(fromBodyBuilder.build())
//                        .build()


//                    coroutineCallInternet.async {
//
//                       // LoggerBird.saveRetrofitRequestDetails()
//                    }
//                    for (i in 0..10) {
//                        LoggerBird.callRetrofitRequestDetails(
//                            response = ApiServiceInterface.httpClient(
//                                request
//                            ), request = request
//                        )
//                    }


                    //  LogDeneme.saveRetrofitRequestDetails()
                    //  LogDeneme.saveAllDetails(response=ApiServiceInterface.httpClient(request),context = context,request=request)

                }
            })
        }


    }

    private fun checkEmpty(): Boolean {
        follows = editText_follows.text.toString();
        followers = editText_followers.text.toString()
        if (follows.length != 0 || followers.length != 0) {
            return true

        } else {
            Toast.makeText(
                this,
                getString(R.string.Main_Activity_edit_text_empty),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
    }

    private fun writeTextFile() {
        try {
            readLogs()
            val filePath = this.getFilesDir()
            val fileDirectory = File(filePath, "example")
            fileDirectoryException = fileDirectory
            // LogDeneme.saveAllDetails(fileName ="berk_deneme" ,context = this,view = button_read_logs,resources = resources)
            // fileDirectory.mkdir()


            filePath.createNewFile()
            FileOutputStream(filePath).use {
                filePath.appendText(logs)
            }
            Toast.makeText(this, "File Created Successfully!", Toast.LENGTH_SHORT).show()
            getFilePath = filePath.absolutePath
            readTextFile()
        } catch (e: Exception) {
            e.printStackTrace()
//            LogDeneme.saveExceptionDetails(null,null,this)

        }

    }

    private fun readTextFile() {
        try {
            val file = File(getFilePath)
            file.readText()
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(file.readText())
            alertDialogBuilder.show()
            // Toast.makeText(this,file.readText(),Toast.LENGTH_SHORT).show()

            val intentFile = Intent()
            intentFile.setAction(Intent.ACTION_VIEW)
            intentFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intentFile.setData(Uri.parse(getFilePath))
            startActivityForResult(
                Intent.createChooser(intentFile, "View Txt File!"),
                FILE_TEXT_REQUEST_CODE
            )


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun permissions(): Boolean {
        val controlWriteStoragePermission: Int = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val controlReadStoragePermission: Int = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val controlInternetPermission: Int =
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET)
        val controlNetworkPermission: Int = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_NETWORK_STATE
        )
//        val controlNetworkPermission: Int = ContextCompat.checkSelfPermission(
//            this,
//            android.Manifest.permission.ACCESS_NETWORK_STATE
//        )
        if (controlWriteStoragePermission == PackageManager.PERMISSION_GRANTED && controlReadStoragePermission == PackageManager.PERMISSION_GRANTED && controlInternetPermission == PackageManager.PERMISSION_GRANTED && controlNetworkPermission == PackageManager.PERMISSION_GRANTED) {
            return true
        } else if (controlWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_STORAGE_REQUEST_CODE
            )
            permissions()
        } else if (controlReadStoragePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                Read_STORAGE_REQUEST_CODE
            )
            permissions()
        } else if (controlInternetPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.INTERNET),
                INTERNET_REQUEST_CODE
            )
            permissions()
        } else if (controlNetworkPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_NETWORK_STATE),
                NETWORK_REQUEST_CODE
            )
            permissions()
        }
        return false
    }

    private fun readLogs() {
        try {
            var txtCounter: Int = 0
            val process = Runtime.getRuntime().exec("logcat -d")
            val bufferedReader = BufferedReader(
                InputStreamReader(process.inputStream)
            )
            val log = StringBuilder()
            log.append("Logcat:" + "\n")
            do {
                if (bufferedReader.readLine() == null) {
                    break;
                }
                log.append(bufferedReader.readLine() + "\n")
                txtCounter++

            } while (txtCounter < 250)
            logs = log.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun openDocuments() {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.action = Intent.ACTION_OPEN_DOCUMENT
        }
        intent.type = "*/*"
        startActivityForResult(
            Intent.createChooser(intent, "select file"),
            Read_STORAGE_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LoggerBird.onActivityResult(requestCode = requestCode, resultCode = resultCode, data = data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Read_STORAGE_REQUEST_CODE) {
                val fileUri: Uri
                if (data != null) {
                    fileUri = data.data!!
                    Toast.makeText(this, "file path" + fileUri.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        LoggerBird.onRequestPermissionResult(requestCode=requestCode , permissions = permissions , grantResults = grantResults)
//    }

    override fun onStart() {
        super.onStart()
        Log.d("life_cycle_state_start", this.lifecycle.currentState.name)
    }

    override fun onPause() {
        super.onPause()
        Log.d("life_cycle_state_pause", this.lifecycle.currentState.name)
    }

    override fun onStop() {
        super.onStop()
    }


    private suspend fun httpRequest(url: String?): String {
        var result: String = ""
        val inputStream: InputStream
        val getUrl: URL = URL(url)
        try {
            val internetConnection: HttpURLConnection = getUrl.openConnection() as HttpURLConnection

            internetConnection.connect()

            inputStream = internetConnection.inputStream

            if (inputStream != null) {
//                val inputStreamReader = InputStreamReader(inputStream)
//                val bufferedReader = BufferedReader(inputStreamReader)
//                val stringBuffer = StringBuffer()
//                while (bufferedReader.readLine() != null) {
//                    result = stringBuffer.append(bufferedReader.readLine()).toString()
//                }

                Log.d("http", "Request Code:" + internetConnection.responseCode)
                result = Integer.toString(internetConnection.responseCode)
                if (result == "200") {
                    implementRealm()


                    //   Toast.makeText(this,"Http Request Code:200",Toast.LENGTH_SHORT).show()
                    coroutineCallInternet.async { insertRealm() }
//                    insertRealm()
                }

            } else {
                Log.d("http", "Request Failure with:" + inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        return result
    }

    private fun implementRealm() {
        try {
//            Looper.prepare()
            Realm.init(this)
            val realmConfig =
                RealmConfiguration.Builder().name("filedenemeleredevamlarasde.realm").build()
            Realm.setDefaultConfiguration(realmConfig)
            realmInstance = Realm.getDefaultInstance()
//            val realmItemCreation=realmInstance.createObject(RealmItem::class.java,UUID.randomUUID().toString())
            takeData()
//            insertRealm()
        } catch (e: Exception) {
            e.printStackTrace()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun insertRealm(): Boolean {
//        Realm.init(this)
//        val realmConfig = RealmConfiguration.Builder().name("filedenemeleredevamlara.realm").build()
//        Realm.setDefaultConfiguration(realmConfig)
//        realmInstanceInsert= Realm.getDefaultInstance()

        //val realmItemCreation=realmInstance.createObject(RealmItem::class.java,UUID.randomUUID().toString())

//        realmItemCreation.follower=0
//        realmItemCreation.follows=0
//        realmInstance.insert(realmItemCreation)
        try {
            counterlist.add(RealmItem(follows))
            counterlist.add(RealmItem(followers))
            // realmInstance.copyToRealmOrUpdate(counterlist)

//            realmInstanceInsert.createObject(RealmItem::class.java,UUID.randomUUID().toString())
            realmInstance.beginTransaction()
            realmInstance.insert(counterlist)
            realmInstance.commitTransaction()
//            realmInstanceInsert.close()
            readRealmData()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        /*   realmInstance.executeTransactionAsync({
               val realmItemCreation = it.createObject(RealmItem::class.java, UUID.randomUUID().toString())
   //            realmItemCreation.follower=Integer.parseInt(followers)
   //            realmItemCreation.follows=Integer.parseInt(follows)
               realmItemCreation.follower = 0
               realmItemCreation.follows = 0
   //            realmInstance.insert(realmItemCreation)
               realmInstance.copyToRealmOrUpdate(realmItemCreation)
               readRealmData()
           }, {
               Toast.makeText(
                   this,
                   getString(R.string.Main_activity_Insert_Successfull),
                   Toast.LENGTH_SHORT
               ).show()

   //            Looper.loop()
           }, {
               Toast.makeText(
                   this,
                   getString(R.string.Main_activity_Insert_Failure),
                   Toast.LENGTH_SHORT
               ).show()
   //            Looper.loop()
           })*/

        return true
    }

    private fun takeData() {
        realmInstance.beginTransaction()
        var realmItemCreation =
            realmInstance.where(RealmItem::class.java).sort("id", Sort.DESCENDING).findFirst()
        if (realmItemCreation != null) {
            if (realmItemCreation.follows != null) {
                textView_follows.text = realmItemCreation.follows
            }
            if (realmItemCreation.follower != null) {
                if (realmItemCreation.follower != null) {
                    textView_followers.text = realmItemCreation.follower
                }
            }
        }

        realmInstance.commitTransaction()
//        LogDeneme.logRealmDetails(realm = realmInstance)
//        LogDeneme.saveRealmDetails()


    }

    private fun readRealmData() {
        //   val realmRead=realmInstance.where(RealmItem::class.java).findAll()
        try {
//            var mHandler: Handler? = Handler()
//            val realmReadFollows = realmInstance.copyFromRealm(RealmItem())
//            val realmReadFollowers = realmInstance.copyFromRealm(RealmItem())
//            if (realmReadFollows.follows != null) {
//                textView_follows.setText(realmReadFollows.follows!!)
//            }
//            if (realmReadFollowers.follower != null) {
//                textView_followers.setText(realmReadFollows.follower!!)
//            }
//            Looper.prepareMainLooper()
//            Looper.prepare()
//            if (mHandler != null) {
//                Handler().post{
//                    textView_follows.text=follows
//                    textView_followers.text=followers
////                }
//            }


            this@MainActivity.runOnUiThread(java.lang.Runnable {
                textView_follows.text = follows
                textView_followers.text = followers
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun saveComponentDetails(
        filePath: File,
        view: View,
        resources: Resources,
        context: Context
    ): Boolean {
        try {
            filePath.createNewFile()
            FileOutputStream(filePath).use {
                filePath.appendText(takeComponentDetails(view, resources))
                Toast.makeText(context, "file saved successfully", Toast.LENGTH_SHORT).show()
                Toast.makeText(context, takeComponentDetails(view, resources), Toast.LENGTH_LONG)
                    .show()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun takeComponentDetails(view: View, resources: Resources): String {
        stringBuilderComponent.append(
            "Transaction" + "Component Details" + "\n" + "Component Name:" + resources.getResourceName(
                view.id
            ) + "Component Id:" + view.id
        )
        return stringBuilderComponent.toString()
    }


    class AsyncHttpRequst(val url: String) : AsyncTask<Void, Void, String>() {


        override fun doInBackground(vararg params: Void?): String? {
            // ...

            var result: String = ""
            val inputStream: InputStream
            val getUrl: URL = URL(url)

            try {
                val internetConnection: HttpURLConnection =
                    getUrl.openConnection() as HttpURLConnection

                internetConnection.connect()

                inputStream = internetConnection.inputStream

                if (inputStream != null) {
                    val stringBuffer = StringBuffer()
                    val inputStreamReader = InputStreamReader(inputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)

                    while (bufferedReader.readLine() != null) {
                        result = stringBuffer.append(bufferedReader.readLine()).toString()
                    }

                } else {
                    Log.d("http", "Request Failure with:" + inputStream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


            return result
        }

        override fun onPreExecute() {
            super.onPreExecute()
            // ...
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            // ...
        }


    }
}
