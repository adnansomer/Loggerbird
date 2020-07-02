package utils

import adapter.RecyclerViewBasecampAdapter
import adapter.RecyclerViewBasecampAssigneeAdapter
import adapter.RecyclerViewBasecampNotifyAdapter
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
import kotlin.collections.HashMap

internal class BasecampAuthentication {
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
                    okHttpPivotalAuthentication(
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

    private fun okHttpPivotalAuthentication(
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
            RetrofitUserBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/buckets/$projectId/message_boards/$messageBoardId/")
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
                        RecyclerViewBasecampAdapter.ViewHolder.arrayListFilePaths.forEach {
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

    private fun basecampCreateTodoIssue(activity: Activity, projectId: String, accountId: String) =
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
            RetrofitUserBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/buckets/$projectId/todosets/$todoId/")
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
            RetrofitUserBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/buckets/$projectId/todolists/$todoListId/")
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

    private fun gatherTaskAccountId() {
        queueCounter++
        RetrofitUserBasecampClient.getBasecampUserClient(url = "https://launchpad.37signals.com/")
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

    private fun gatherTaskProject(accountId: String) {
        queueCounter++
        RetrofitUserBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/")
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
                            this@BasecampAuthentication.accountId = accountId
//                            basecampList!!.getAsJsonArray("accounts").asJsonArray[0].asJsonObject["id"].asString
                            this@BasecampAuthentication.messageBoardId =
                                basecampList!!.asJsonArray[projectPosition].asJsonObject["dock"].asJsonArray[0].asJsonObject["id"].asString
                            this@BasecampAuthentication.todoId =
                                basecampList!!.asJsonArray[projectPosition].asJsonObject["dock"].asJsonArray[1].asJsonObject["id"].asString
                            this@BasecampAuthentication.vaultId =
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

    private fun gatherTaskAssignee(accountId: String, projectId: String) {
        queueCounter++
        RetrofitUserBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/projects/$projectId/")
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
                    val coroutineCallBasecampProject = CoroutineScope(Dispatchers.IO)
                    coroutineCallBasecampProject.async {
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

    private fun gatherTaskCategory(accountId: String, projectId: String) {
        queueCounter++
        RetrofitUserBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/buckets/$projectId/")
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
                    val coroutineCallBasecampProject = CoroutineScope(Dispatchers.IO)
                    coroutineCallBasecampProject.async {
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


    private fun updateFields() {
        queueCounter--
        Log.d("que_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeBasecampAutoTextViews(
                    arrayListProject = arrayListProjectNames,
                    arrayListAssignee = arrayListAssigneeNames,
                    arrayListCategory = arrayListCategoryNames,
                    arrayListCategoryIcon = arrayListCategoryIcon,
                    arrayListNotify = arrayListNotifyNames
                )
            }
        }

    }


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

    internal fun setProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }

    internal fun setCategoryPosition(categoryPosition: Int) {
        this.categoryPosition = categoryPosition
    }

    internal fun setStartDate(startDate: String?) {
        this.startDate = startDate
    }

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

    private fun createAttachments(
        accountId: String,
        file: File,
        activity: Activity
    ) {
        try {
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name.substringBeforeLast("."), requestFile)
            RetrofitUserBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/")
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
                                fileName = file.name,
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
            RetrofitUserBasecampClient.getBasecampUserClient(url = "https://3.basecampapi.com/$accountId/buckets/$projectId/vaults/$vaultId/")
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkBasecampProject(
        activity: Activity,
        autoTextViewBasecampProject: AutoCompleteTextView
    ): Boolean {
        if (autoTextViewBasecampProject.editableText.toString().isNotEmpty() && arrayListProjectNames.contains(
                autoTextViewBasecampProject.editableText.toString()
            )
        ) {
            return true
        } else if (autoTextViewBasecampProject.editableText.toString().isEmpty()) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.basecamp_project_empty)
            )
        } else if (!arrayListProjectNames.contains(autoTextViewBasecampProject.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.basecamp_project_doesnt_exist)
            )
        }
        return false
    }

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
                            autoTextViewBasecampCategory = autoTextViewCategory
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

    private fun checkBasecampCategory(
        activity: Activity,
        autoTextViewBasecampCategory: AutoCompleteTextView
    ): Boolean {
        if (arrayListCategoryNames.contains(autoTextViewBasecampCategory.editableText.toString()) || autoTextViewBasecampCategory.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.basecamp_category_doesnt_exist)
            )
        }
        return false
    }

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
        if(editTextName.text.toString().isEmpty()){
            if(editTextContent.text.toString().isNotEmpty() || autoTextViewAssignee.editableText.toString().isNotEmpty() || autoTextViewNotify.editableText.isNotEmpty()){
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.basecamp_name_empty)
                )
                return false
            }
        }
        return true
    }

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