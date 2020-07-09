package utils.api.basecamp

import adapter.recyclerView.api.basecamp.RecyclerViewBasecampAttachmentAdapter
import adapter.recyclerView.api.basecamp.RecyclerViewBasecampAssigneeAdapter
import adapter.recyclerView.api.basecamp.RecyclerViewBasecampNotifyAdapter
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import loggerbird.LoggerBird
import models.AccountIdService
import okhttp3.*
import services.LoggerBirdService
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import utils.other.DefaultToast
import utils.other.InternetConnectionUtil
import kotlin.collections.HashMap
/** Loggerbird Clubhouse api configration class **/
internal class BasecampApi {
    //Global variables.
    private lateinit var activity: Activity
    private lateinit var context: Context
    private var filePathMedia: File? = null
    private val coroutineCallOkHttpBasecamp = CoroutineScope(Dispatchers.IO)
    private val coroutineCallBasecamp = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private var queueCounter = 0
    private lateinit var timerTaskQueue: TimerTask
    private val arrayListProjectNames: ArrayList<String> = ArrayList()
    private val arrayListProjectId: ArrayList<String> = ArrayList()
    private val arrayListAssigneeNames: ArrayList<String> = ArrayList()
    private val arrayListNotifyNames: ArrayList<String> = ArrayList()
    private val arrayListCategoryNames: ArrayList<String> = ArrayList()
    private val arrayListCategoryIcon: ArrayList<String> = ArrayList()
    private val hashMapCategory: HashMap<String, String> = HashMap()
    private val hashMapAssignee: HashMap<String, String> = HashMap()
    private val hashMapNotify: HashMap<String, String> = HashMap()
    private var projectPosition = 0
    private var categoryPosition = 0
    private var startDate: String? = null
    private var project: String? = null
    private var projectId: String? = null
    private var accountId: String? = null
    private var vaultId: String? = null
    private var messageBoardId: String? = null
    private var todoId: String? = null
    private var assignee: String? = null
    private var notify: String? = null
    private var category: String? = null
    private var title: String? = null
    private var content: String? = null
    private var name: String? = null
    private var descriptionMessage: String? = null
    private var descriptionTodo: String? = null
    private val defaultToast = DefaultToast()
    /**
     * This method is used for calling an basecamp action with network connection check.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param task is for getting reference of which basecamp action will be executed.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws LoggerBirdException if network connection error occurs.
     * @throws exception if error occurs.
     * @see basecampExceptionHandler method.
     */
    internal fun callBasecamp(
        activity: Activity,
        context: Context,
        task: String,
        filePathMedia: File? = null
    ) {
        this.activity = activity
        this.context = context
        this.filePathMedia = filePathMedia
        coroutineCallOkHttpBasecamp.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    checkQueueTime(activity = activity)
                    okHttpBasecampAuthentication(
                        activity = activity,
                        context = context,
                        task = task,
                        filePathMediaName = filePathMedia
                    )
                } else {
                    activity.runOnUiThread {
                        Toast.makeText(
                            context,
                            R.string.network_check_failure,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    throw LoggerBirdException(
                        Constants.networkErrorMessage
                    )
                }

            } catch (e: Exception) {
                basecampExceptionHandler(e = e)
            }
        }
    }

    /**
     * This method is used for calling an basecamp action with internet connection check.
     * @param context is for getting reference from the application context.
     * @param activity is used for getting reference of current activity.
     * @param task is for getting reference of which basecamp action will be executed.
     * @param filePathMediaName is used for getting the reference of current media file.
     * @throws LoggerBirdException if internet connection error occurs.
     * @throws exception if error occurs.
     * @see basecampExceptionHandler method.
     */
    private fun okHttpBasecampAuthentication(
        context: Context,
        activity: Activity,
        task: String,
        filePathMediaName: File?
    ) {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://3.basecampapi.com")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                basecampExceptionHandler(e = e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("response_message", response.message)
                Log.d("response_code", response.code.toString())
                try {
                    if (response.code in 200..299) {
                        coroutineCallBasecamp.async {
                            try {
                                when (task) {
                                    "create" -> basecampCreateMessage(activity = activity)
                                    "get" -> gatherBasecampDetails()
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                LoggerBird.callEnqueue()
                                LoggerBird.callExceptionDetails(
                                    exception = e,
                                    tag = Constants.basecampTag
                                )
                            }
                        }
                    } else {
                        activity.runOnUiThread {
                            Toast.makeText(
                                context,
                                R.string.internet_connection_check_failure,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        throw LoggerBirdException(
                            Constants.internetErrorMessage
                        )
                    }
                } catch (e: Exception) {
                    basecampExceptionHandler(e = e)
                }
            }
        })
    }

    /**
     * This method is used for creating basecamp message.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs.
     * @see basecampExceptionHandler method.
     */
    private fun basecampCreateMessage(activity: Activity) {
        try {
            projectId = arrayListProjectId[projectPosition]
            queueCounter = 0
            queueCounter++
            this.activity = activity
            val jsonObject = JsonObject()
            if (!name.isNullOrEmpty()) {
                basecampCreateTodoIssue(
                    activity = activity,
                    projectId = projectId!!,
                    accountId = accountId!!
                )
            }
            if (!category.isNullOrEmpty()) {
                jsonObject.addProperty(
                    "category_id",
                    hashMapCategory[arrayListCategoryNames[categoryPosition]]?.toInt()
                )
            }
            if (!title.isNullOrEmpty()) {
                jsonObject.addProperty("subject", title)
            }
            if (!descriptionMessage.isNullOrEmpty()) {
                jsonObject.addProperty("content", descriptionMessage)
            }
            jsonObject.addProperty("status", "active")
            RetrofitBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/buckets/$projectId/message_boards/$messageBoardId/")
                .create(AccountIdService::class.java)
                .createBasecampMessage(
                    jsonObject = jsonObject,
                    accessToken = LoggerBird.basecampApiToken
                )
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        basecampExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        val coroutineCallBasecampAttachments = CoroutineScope(Dispatchers.IO)
                        Log.d("basecamp_details", response.code().toString())
                        val basecampList = response.body()
                        RecyclerViewBasecampAttachmentAdapter.ViewHolder.arrayListFilePaths.forEach {
                            queueCounter++
                            coroutineCallBasecampAttachments.async {
                                createAttachments(
                                    activity = activity,
                                    file = it.file,
                                    accountId = accountId!!
                                )
                            }
                        }
                        resetBasecampValues(shareLayoutMessage = "basecamp")
                    }
                })

        } catch (e: Exception) {
            basecampExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for creating basecamp to-do.
     * @param activity is used for getting reference of current activity.
     * @param projectId is used for getting reference of id of selected project in project field which contained in project layout.
     * @param accountId is used for getting reference of id of authenticated user.
     * @throws exception if error occurs.
     * @see basecampExceptionHandler method.
     */
    private fun basecampCreateTodoIssue(activity: Activity, projectId: String, accountId: String) {
        try {
            queueCounter++
            this.activity = activity
            val jsonObject = JsonObject()
            if (!name.isNullOrEmpty()) {
                jsonObject.addProperty("name", name)
            }
            if (!descriptionTodo.isNullOrEmpty()) {
                jsonObject.addProperty("description", descriptionTodo)
            }
            RetrofitBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/buckets/$projectId/todosets/$todoId/")
                .create(AccountIdService::class.java)
                .createBasecampTodo(
                    jsonObject = jsonObject,
                    accessToken = LoggerBird.basecampApiToken
                )
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        basecampExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        Log.d("basecamp_todo_details", response.code().toString())
                        val basecampList = response.body()
                        if (basecampList != null) {
                            queueCounter++
                            val coroutineBaseCampList = CoroutineScope(Dispatchers.IO)
                            coroutineBaseCampList.async {
                                basecampAddTodo(
                                    activity = activity,
                                    projectId = projectId,
                                    accountId = accountId,
                                    todoListId = basecampList.getAsJsonPrimitive("id").asString
                                )
                            }
                        }
                        resetBasecampValues(shareLayoutMessage = "basecamp")
                    }
                })

        } catch (e: Exception) {
            basecampExceptionHandler(e = e)
        }
    }


    /**
     * This method is used for adding basecamp to-do.
     * @param activity is used for getting reference of current activity.
     * @param projectId is used for getting reference of id of selected project in project field which contained in project layout.
     * @param accountId is used for getting reference of id of authenticated user.
     * @param todoListId is used for getting reference of id of created basecamp to-do.
     * @throws exception if error occurs.
     * @see basecampExceptionHandler method.
     */
    private fun basecampAddTodo(
        activity: Activity,
        projectId: String,
        accountId: String,
        todoListId: String
    ) {
        try {
            this.activity = activity
            val jsonObject = JsonObject()
            val jsonArrayAssignee = JsonArray()
            if (RecyclerViewBasecampAssigneeAdapter.ViewHolder.arrayListAssignee.isNotEmpty()) {
                RecyclerViewBasecampAssigneeAdapter.ViewHolder.arrayListAssignee.forEach {
                    jsonArrayAssignee.add(hashMapAssignee[it.assigneeName]?.toInt())
                }
                jsonObject.add("assignee_ids", jsonArrayAssignee)
            } else {
                if (!assignee.isNullOrEmpty()) {
                    jsonArrayAssignee.add(hashMapAssignee[assignee!!]?.toInt())
                    jsonObject.add("assignee_ids", jsonArrayAssignee)
                }
            }
            val jsonArrayNotify = JsonArray()
            if (RecyclerViewBasecampNotifyAdapter.ViewHolder.arrayListNotify.isNotEmpty()) {
                RecyclerViewBasecampNotifyAdapter.ViewHolder.arrayListNotify.forEach {
                    jsonArrayNotify.add(hashMapNotify[it.notifyName]?.toInt())
                }
                jsonObject.add("completion_subscriber_ids", jsonArrayNotify)
                jsonObject.addProperty("notify", true)
            } else {
                if (!notify.isNullOrEmpty()) {
                    jsonArrayNotify.add(hashMapNotify[notify!!]?.toInt())
                    jsonObject.add("completion_subscriber_ids", jsonArrayNotify)
                    jsonObject.addProperty("notify", true)
                }
            }
            if (!startDate.isNullOrEmpty()) {
                jsonObject.addProperty("due_on", startDate)
            }
            if (!content.isNullOrEmpty()) {
                jsonObject.addProperty("content", content)
            }
            RetrofitBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/buckets/$projectId/todolists/$todoListId/")
                .create(AccountIdService::class.java)
                .addBasecampTodo(
                    jsonObject = jsonObject,
                    accessToken = LoggerBird.basecampApiToken
                )
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        basecampExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        Log.d("basecamp_list_details", response.code().toString())
                        val basecampList = response.body()
                        resetBasecampValues(shareLayoutMessage = "basecamp")
                    }
                })

        } catch (e: Exception) {
            basecampExceptionHandler(e = e)
        }
    }


    /**
     * This method is used for creating basecamp attachment when basecamp message created.
     * @param accountId is used for getting reference of id of authenticated user.
     * @param file is used for getting reference of the current file.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs.
     * @see basecampExceptionHandler method.
     */
    private fun createAttachments(
        accountId: String,
        file: File,
        activity: Activity
    ) {
        try {
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            RetrofitBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/")
                .create(AccountIdService::class.java)
                .setBasecampAttachments(
                    file = body,
                    accessToken = LoggerBird.basecampApiToken,
                    name = file.name,
                    contentLength = file.length()
                )
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        basecampExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        queueCounter++
                        val coroutineCallBasecampAttachments = CoroutineScope(Dispatchers.IO)
                        coroutineCallBasecampAttachments.async {
                            if (file.name != "logger_bird_details.txt") {
                                if (file.exists()) {
                                    file.delete()
                                }
                            }
                            val basecampResponse = response.body()
                            Log.d("attachment_put_success", response.code().toString())
                            Log.d("attachment_put_success", response.message())
                            addAttachments(
                                activity = activity,
                                fileName = file.name.substringBeforeLast("."),
                                accountId = accountId,
                                projectId = projectId!!,
                                attachmentId = basecampResponse!!.getAsJsonPrimitive("attachable_sgid").asString
                            )
                            resetBasecampValues(shareLayoutMessage = "basecamp")
                        }
                    }
                })
        } catch (e: Exception) {
            basecampExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for adding basecamp attachment when basecamp attachment created.
     * @param activity is used for getting reference of current activity.
     * @param fileName is used for getting reference of the current file name.
     * @param accountId is used for getting reference of id of authenticated user.
     * @param projectId is used for getting reference of id of selected project in project field which contained in project layout.
     * @param attachmentId is used for getting reference of id of created attachment.
     * @throws exception if error occurs.
     * @see basecampExceptionHandler method.
     */
    private fun addAttachments(
        activity: Activity,
        fileName: String,
        accountId: String,
        projectId: String,
        attachmentId: String
    ) {
        try {
            val jsonObject = JsonObject()
            jsonObject.addProperty("base_name", fileName)
            jsonObject.addProperty("attachable_sgid", attachmentId)
            RetrofitBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/buckets/$projectId/vaults/$vaultId/")
                .create(AccountIdService::class.java)
                .addBaseAttachments(
                    jsonObject = jsonObject,
                    accessToken = LoggerBird.basecampApiToken
                )
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        resetBasecampValues(shareLayoutMessage = "basecamp_error")
                        basecampExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        Log.d("attachment_put_sucess", response.code().toString())
                        Log.d("attachment_put_sucess", response.message())
                        resetBasecampValues(shareLayoutMessage = "basecamp")
                    }
                })
        } catch (e: Exception) {
            basecampExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for initializing the gathering action of basecamp.
     * @throws exception if error occurs.
     * @see basecampExceptionHandler method.
     */
    private fun gatherBasecampDetails() {
        try {
            queueCounter = 0
            arrayListCategoryNames.clear()
            arrayListCategoryIcon.clear()
            arrayListNotifyNames.clear()
            arrayListAssigneeNames.clear()
            arrayListProjectNames.clear()
            arrayListProjectId.clear()
            hashMapCategory.clear()
            hashMapAssignee.clear()
            hashMapNotify.clear()
            gatherTaskAccountId()
        } catch (e: Exception) {
            basecampExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for getting authenticated user account details for basecamp.
     * @throws exception if error occurs.
     * @see basecampExceptionHandler method.
     */
    private fun gatherTaskAccountId() {
        queueCounter++
        RetrofitBasecampClient.getBasecampUserClient(url = "https://launchpad.37signals.com/")
            .create(AccountIdService::class.java)
            .getBasecampProjectId(accessToken = LoggerBird.basecampApiToken)
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    basecampExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    Log.d("base_account_id_success", response.code().toString())
                    val basecampList = response.body()
                    val coroutineCallBasecampProject = CoroutineScope(Dispatchers.IO)
                    coroutineCallBasecampProject.async {
                        gatherTaskProject(accountId = basecampList!!.getAsJsonArray("accounts").asJsonArray[0].asJsonObject["id"].asString)
                        updateFields()
                    }
                }
            })
    }

    /**
     * This method is used for getting project details for basecamp.
     * @param accountId is used for getting reference of id of authenticated user.
     * @throws exception if error occurs.
     * @see basecampExceptionHandler method.
     */
    private fun gatherTaskProject(accountId: String) {
        queueCounter++
        RetrofitBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/")
            .create(AccountIdService::class.java)
            .getBasecampProjects(accessToken = LoggerBird.basecampApiToken)
            .enqueue(object : retrofit2.Callback<JsonArray> {
                override fun onFailure(
                    call: retrofit2.Call<JsonArray>,
                    t: Throwable
                ) {
                    basecampExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonArray>,
                    response: retrofit2.Response<JsonArray>
                ) {
                    val coroutineCallBasecampProject = CoroutineScope(Dispatchers.IO)
                    coroutineCallBasecampProject.async {
                        Log.d("base_project_success", response.code().toString())
                        val basecampList = response.body()
                        basecampList?.forEach {
                            if (it.asJsonObject["name"] != null) {
                                arrayListProjectNames.add(it.asJsonObject["name"].asString)
                                arrayListProjectId.add(it.asJsonObject["id"].asString)
                            }
                        }
                        if (arrayListProjectId.size > projectPosition) {
                            this@BasecampApi.accountId = accountId
//                            basecampList!!.getAsJsonArray("accounts").asJsonArray[0].asJsonObject["id"].asString
                            this@BasecampApi.messageBoardId =
                                basecampList!!.asJsonArray[projectPosition].asJsonObject["dock"].asJsonArray[0].asJsonObject["id"].asString
                            this@BasecampApi.todoId =
                                basecampList!!.asJsonArray[projectPosition].asJsonObject["dock"].asJsonArray[1].asJsonObject["id"].asString
                            this@BasecampApi.vaultId =
                                basecampList!!.asJsonArray[projectPosition].asJsonObject["dock"].asJsonArray[2].asJsonObject["id"].asString
                            gatherTaskAssignee(
                                accountId = accountId,
                                projectId = arrayListProjectId[projectPosition]
                            )
                            gatherTaskCategory(
                                accountId = accountId,
                                projectId = arrayListProjectId[projectPosition]
                            )
                        }
                        updateFields()

                    }
                }
            })
    }

    /**
     * This method is used for getting assignee details for basecamp.
     * @param accountId is used for getting reference of id of authenticated user.
     * @param projectId is used for getting reference of current chosen project id.
     * @throws exception if error occurs.
     * @see basecampExceptionHandler method.
     */
    private fun gatherTaskAssignee(accountId: String, projectId: String) {
        queueCounter++
        RetrofitBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/projects/$projectId/")
            .create(AccountIdService::class.java)
            .getBasecampAssignee(accessToken = LoggerBird.basecampApiToken)
            .enqueue(object : retrofit2.Callback<JsonArray> {
                override fun onFailure(
                    call: retrofit2.Call<JsonArray>,
                    t: Throwable
                ) {
                    basecampExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonArray>,
                    response: retrofit2.Response<JsonArray>
                ) {
                    val coroutineCallBasecampAssignee = CoroutineScope(Dispatchers.IO)
                    coroutineCallBasecampAssignee.async {
                        Log.d("base_assignee_success", response.code().toString())
                        val basecampList = response.body()
                        basecampList?.forEach {
                            if (it.asJsonObject["name"] != null) {
                                arrayListAssigneeNames.add(it.asJsonObject["name"].asString)
                                arrayListNotifyNames.add(it.asJsonObject["name"].asString)
                                hashMapAssignee[it.asJsonObject["name"].asString] =
                                    it.asJsonObject["id"].asString
                                hashMapNotify[it.asJsonObject["name"].asString] =
                                    it.asJsonObject["id"].asString
                            }
                        }
                        updateFields()

                    }
                }
            })
    }

    /**
     * This method is used for getting category details for basecamp.
     * @param accountId is used for getting reference of id of authenticated user.
     * @param projectId is used for getting reference of current chosen project id.
     * @throws exception if error occurs.
     * @see basecampExceptionHandler method.
     */
    private fun gatherTaskCategory(accountId: String, projectId: String) {
        queueCounter++
        RetrofitBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/buckets/$projectId/")
            .create(AccountIdService::class.java)
            .getBasecampCategories(accessToken = LoggerBird.basecampApiToken)
            .enqueue(object : retrofit2.Callback<JsonArray> {
                override fun onFailure(
                    call: retrofit2.Call<JsonArray>,
                    t: Throwable
                ) {
                    basecampExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonArray>,
                    response: retrofit2.Response<JsonArray>
                ) {
                    val coroutineCallBasecampCategory = CoroutineScope(Dispatchers.IO)
                    coroutineCallBasecampCategory.async {
                        Log.d("base_category_success", response.code().toString())
                        val basecampList = response.body()
                        basecampList?.forEach {
                            if (it.asJsonObject["name"] != null) {
                                arrayListCategoryNames.add(it.asJsonObject["name"].asString)
                                arrayListCategoryIcon.add(it.asJsonObject["icon"].asString)
                                hashMapCategory[it.asJsonObject["name"].asString] =
                                    it.asJsonObject["id"].asString
                            }
                        }
                        updateFields()

                    }
                }
            })
    }

    /**
     * This method is used for getting details of autoCompleteTextViews in the basecamp layout.
     * @param autoTextViewProject is used for getting project details from project autoCompleteTextView in the basecamp layout.
     * @param autoTextViewAssignee is used for getting assignee details from assignee autoCompleteTextView in the basecamp layout.
     * @param autoTextViewNotify is used for getting notify details from notify autoCompleteTextView in the basecamp layout.
     * @param autoTextViewCategory is used for getting category details from section autoCompleteTextView in the basecamp layout.
     */
    internal fun gatherAutoTextDetails(
        autoTextViewProject: AutoCompleteTextView,
        autoTextViewAssignee: AutoCompleteTextView,
        autoTextViewNotify: AutoCompleteTextView,
        autoTextViewCategory: AutoCompleteTextView
    ) {
        project = autoTextViewProject.editableText.toString()
        assignee = autoTextViewAssignee.editableText.toString()
        notify = autoTextViewNotify.editableText.toString()
        category = autoTextViewCategory.editableText.toString()
    }

    /**
     * This method is used for getting details of editTexts in the basecamp layout.
     * @param editTextTitle is used for getting title details from title editText in the basecamp layout.
     * @param editTextDescriptionMessage is used for getting description message details from description message editText in the basecamp layout.
     * @param editTextDescriptionTodo is used for getting description to-do details from to-do message editText in the basecamp layout.
     * @param editTextContent is used for getting content details from content editText in the basecamp layout.
     * @param editTextName is used for getting name details from name editText in the basecamp layout.
     */
    internal fun gatherEditTextDetails(
        editTextTitle: EditText,
        editTextDescriptionMessage: EditText,
        editTextDescriptionTodo: EditText,
        editTextContent: EditText,
        editTextName: EditText
    ) {
        title = editTextTitle.text.toString()
        descriptionMessage = editTextDescriptionMessage.text.toString()
        descriptionTodo = editTextDescriptionTodo.text.toString()
        content = editTextContent.text.toString()
        name = editTextName.text.toString()
    }

    /**
     * This method is used for updating and controlling the queue of background tasks in the basecamp actions.
     */
    private fun updateFields() {
        queueCounter--
        Log.d("que_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeBasecampAutoTextViews(
                    arrayListBasecampProject = arrayListProjectNames,
                    arrayListBasecampAssignee = arrayListAssigneeNames,
                    arrayListBasecampCategory = arrayListCategoryNames,
                    arrayListBasecampCategoryIcon = arrayListCategoryIcon,
                    arrayListBasecampNotify = arrayListNotifyNames
                )
            }
        }

    }


    /**
     * This method is used for controlling the time of background tasks in the basecamp actions.
     If tasks will last longer than three minutes then basecamp layout will be removed.
     */
    private fun checkQueueTime(activity: Activity) {
        val timerQueue = Timer()
        timerTaskQueue = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread {
                    LoggerBirdService.loggerBirdService.finishShareLayout("basecamp_error_time_out")
                }
            }
        }
        timerQueue.schedule(timerTaskQueue, 180000)
    }

    /**
     * This method is used for default exception handling of basecamp class.
     * @param e is used for getting reference of exception.
     * @param throwable is used for getting reference of throwable.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun basecampExceptionHandler(
        e: Exception? = null,
        throwable: Throwable? = null
    ) {
        resetBasecampValues(shareLayoutMessage = "basecamp_error")
        if (this::timerTaskQueue.isInitialized) {
            timerTaskQueue.cancel()
        }
        LoggerBirdService.loggerBirdService.finishShareLayout("basecamp_error")
        throwable?.printStackTrace()
        e?.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.basecampTag,
            throwable = throwable
        )
    }

    /**
     * This method is used for getting reference of current project position in the project autoCompleteTextView in the basecamp layout.
     * @param projectPosition is used for getting reference of project position.
     */
    internal fun setProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }

    /**
     * This method is used for getting reference of current category position in the category autoCompleteTextView in the category layout.
     * @param categoryPosition is used for getting reference of category position.
     */
    internal fun setCategoryPosition(categoryPosition: Int) {
        this.categoryPosition = categoryPosition
    }

    /**
     * This method is used for getting reference of current date time in the basecamp date layout.
     * @param startDate is used for getting reference of start date.
     */
    internal fun setStartDate(startDate: String?) {
        this.startDate = startDate
    }

    /**
     * This method is used for resetting the values in basecamp action.
     * @param shareLayoutMessage is used for getting reference of the basecamp action message.
     */
    private fun resetBasecampValues(shareLayoutMessage: String) {
        queueCounter--
        Log.d("queue_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            arrayListProjectNames.clear()
            arrayListProjectId.clear()
            arrayListAssigneeNames.clear()
            hashMapNotify.clear()
            hashMapCategory.clear()
            hashMapAssignee.clear()
            arrayListNotifyNames.clear()
            arrayListCategoryNames.clear()
            arrayListCategoryIcon.clear()
            projectPosition = 0
            categoryPosition = 0
            project = null
            projectId = null
            startDate = null
            accountId = null
            vaultId = null
            messageBoardId = null
            todoId = null
            assignee = null
            notify = null
            category = null
            title = null
            descriptionMessage = null
            descriptionTodo = null
            content = null
            name = null
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.finishShareLayout(shareLayoutMessage)
            }
        }
    }

    /**
     * This method is used for checking project reference exist in the project list or not empty in the project AutoCompleteTextView field in the basecamp layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewProject is used for getting reference of project autoCompleteTextView in the basecamp layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkBasecampProject(
        activity: Activity,
        autoTextViewProject: AutoCompleteTextView
    ): Boolean {
        if (autoTextViewProject.editableText.toString().isNotEmpty() && arrayListProjectNames.contains(
                autoTextViewProject.editableText.toString()
            )
        ) {
            return true
        } else if (autoTextViewProject.editableText.toString().isEmpty()) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.basecamp_project_empty)
            )
        } else if (!arrayListProjectNames.contains(autoTextViewProject.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.basecamp_project_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking editTextTitle is not empty if title editText,category autoCompleteTexView or description message editText is not empty in the basecamp layout.
     * @param activity is used for getting reference of current activity.
     * @param editTextTitle is used for getting reference of title editText in the basecamp layout.
     * @param autoTextViewCategory is used for getting reference of category autoCompleteTextView in the basecamp layout.
     * @param editTextDescriptionMessage is used for getting reference of description message editText in the basecamp layout.
     * @return Boolean value.
     */
    internal fun checkBasecampTitle(
        activity: Activity,
        editTextTitle: EditText,
        autoTextViewCategory: AutoCompleteTextView,
        editTextDescriptionMessage: EditText
    ): Boolean {
        if (editTextTitle.text.isNotEmpty() || autoTextViewCategory.editableText.toString().isNotEmpty() || editTextDescriptionMessage.text.toString().isNotEmpty()) {
            return if (editTextTitle.text.toString().isNotEmpty()) {
                if (autoTextViewCategory.editableText.toString().isNotEmpty()) {
                    if (!checkBasecampCategory(
                            activity = activity,
                            autoTextViewCategory = autoTextViewCategory
                        )
                    ) {
                        return false
                    }
                }
                true
            } else {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.basecamp_title_empty)
                )
                false
            }
        }
        return true
    }

    /**
     * This method is used for checking category reference exist in the category list or not empty in the category AutoCompleteTextView field in the basecamp layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewCategory is used for getting reference of category autoCompleteTextView in the basecamp layout.
     * @return Boolean value.
     */
    private fun checkBasecampCategory(
        activity: Activity,
        autoTextViewCategory: AutoCompleteTextView
    ): Boolean {
        if (arrayListCategoryNames.contains(autoTextViewCategory.editableText.toString()) || autoTextViewCategory.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.basecamp_category_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking editTextName or editTextDescriptionTodo is not empty if name editText,description to-do editText,content editText is not empty in the basecamp layout.
     * @param activity is used for getting reference of current activity.
     * @param editTextName is used for getting reference of name editText in the basecamp layout.
     * @param editTextDescriptionTodo is used for getting reference of description to-do editText in the basecamp layout.
     * @param editTextContent is used for getting reference of content editText in the basecamp layout.
     * @param autoTextViewAssignee is used for getting reference of assignee autoCompleteTextView in the basecamp layout.
     * @param autoTextViewNotify is used for getting reference of notify autoCompleteTextView in the basecamp layout.
     * @return Boolean value.
     */
    internal fun checkBasecampTodo(
        activity: Activity,
        editTextName: EditText,
        editTextDescriptionTodo: EditText,
        editTextContent: EditText,
        autoTextViewAssignee: AutoCompleteTextView,
        autoTextViewNotify: AutoCompleteTextView
    ): Boolean {
        if (editTextName.text.toString().isNotEmpty() || editTextDescriptionTodo.text.toString().isNotEmpty()) {
            return if (editTextName.text.toString().isNotEmpty()) {
                return checkBasecampTodoList(
                    activity = activity,
                    editTextContent = editTextContent,
                    autoTextViewAssignee = autoTextViewAssignee,
                    autoTextViewNotify = autoTextViewNotify
                )
            } else {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.basecamp_name_empty)
                )
                false
            }
        }
        if (editTextName.text.toString().isEmpty()) {
            if (editTextContent.text.toString().isNotEmpty() || autoTextViewAssignee.editableText.toString().isNotEmpty() || autoTextViewNotify.editableText.isNotEmpty()) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.basecamp_name_empty)
                )
                return false
            }
        }
        return true
    }

    /**
     * This method is used for checking editTextContent,autoTextViewAssignee or autoTextViewNotify is not empty in the basecamp layout.
     * @param activity is used for getting reference of current activity.
     * @param editTextContent is used for getting reference of content editText in the basecamp layout.
     * @param autoTextViewAssignee is used for getting reference of assignee autoCompleteTextView in the basecamp layout.
     * @param autoTextViewNotify is used for getting reference of notify autoCompleteTextView in the basecamp layout.
     * @return Boolean value.
     */
    private fun checkBasecampTodoList(
        activity: Activity,
        editTextContent: EditText,
        autoTextViewAssignee: AutoCompleteTextView,
        autoTextViewNotify: AutoCompleteTextView
    ): Boolean {
        if (editTextContent.text.toString().isNotEmpty() || autoTextViewAssignee.editableText.toString().isNotEmpty() || autoTextViewNotify.editableText.toString().isNotEmpty()) {
            return if (editTextContent.text.toString().isNotEmpty()) {
                if (autoTextViewAssignee.editableText.toString().isNotEmpty()) {
                    if (!arrayListAssigneeNames.contains(autoTextViewAssignee.editableText.toString())) {
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = activity.resources.getString(R.string.basecamp_assignee_doesnt_exist)
                        )
                        return false
                    }
                }
                if (autoTextViewNotify.editableText.toString().isNotEmpty()) {
                    if (!arrayListNotifyNames.contains(autoTextViewNotify.editableText.toString())) {
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = activity.resources.getString(R.string.basecamp_notify_doesnt_exist)
                        )
                        return false
                    }
                }
                true
            } else {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.basecamp_content_empty)
                )
                false
            }
        }
        return true
    }
}