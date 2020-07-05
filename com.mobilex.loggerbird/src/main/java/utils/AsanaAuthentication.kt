package utils

import adapter.RecyclerViewAsanaAdapter
import adapter.RecyclerViewAsanaSubTaskAdapter
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

internal class AsanaAuthentication {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private var filePathMedia: File? = null
    private val coroutineCallOkHttpAsana = CoroutineScope(Dispatchers.IO)
    private val coroutineCallAsana = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private var queueCounter = 0
    private lateinit var timerTaskQueue: TimerTask
    private val arrayListProjectNames: ArrayList<String> = ArrayList()
    private val arrayListProjectId: ArrayList<String> = ArrayList()
    private val arrayListAssigneeNames: ArrayList<String> = ArrayList()
    private val arrayListAssigneeId: ArrayList<String> = ArrayList()
    private val arrayListPriorityNames: ArrayList<String> = ArrayList()
    private val arrayListSectionsNames: ArrayList<String> = ArrayList()
    private val arrayListSectionsId: ArrayList<String> = ArrayList()
    private var projectPosition = 0
    private var sectionPosition = 0
    private var assigneePosition = 0
    private var startDate: String? = null
    private var project: String? = null
    private var projectId: String? = null
    private var assignee: String? = null
    private var priority: String? = null
    private var section: String? = null
    private var description: String? = null
    private var content: String? = null
    private var taskName: String? = null
    private var subtask: String? = null
    private val defaultToast = DefaultToast()
    internal fun callAsana(
        activity: Activity,
        context: Context,
        task: String,
        filePathMedia: File? = null
    ) {
        this.activity = activity
        this.context = context
        this.filePathMedia = filePathMedia
        coroutineCallOkHttpAsana.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    checkQueueTime(activity = activity)
                    okHttpAsanaAuthentication(
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
                asanaExceptionHandler(e = e)
            }
        }
    }

    private fun okHttpAsanaAuthentication(
        context: Context,
        activity: Activity,
        task: String,
        filePathMediaName: File?
    ) {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://asana.com")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                asanaExceptionHandler(e = e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("response_message", response.message)
                Log.d("response_code", response.code.toString())
                try {
                    if (response.code in 200..299) {
                        coroutineCallAsana.async {
                            try {
                                when (task) {
                                    "create" -> asanaCreateMessage(activity = activity)
                                    "get" -> gatherAsanaDetails()
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                LoggerBird.callEnqueue()
                                LoggerBird.callExceptionDetails(
                                    exception = e,
                                    tag = Constants.asanaTag
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
                    asanaExceptionHandler(e = e)
                }
            }
        })
    }

    private fun asanaCreateMessage(activity: Activity) {
        try {
            projectId = arrayListProjectId[projectPosition]
            queueCounter = 0
            queueCounter++
            this.activity = activity
            val jsonObject = JsonObject()
            val jsonObjectData = JsonObject()
            val jsonArrayProjects = JsonArray()
            jsonArrayProjects.add(projectId)
            jsonObjectData.add("projects", jsonArrayProjects)
            jsonObjectData.addProperty("name", taskName)
            if (!assignee.isNullOrEmpty()) {
                jsonObjectData.addProperty("assignee", arrayListAssigneeId[assigneePosition])
            }
            if (!description.isNullOrEmpty()) {
                jsonObjectData.addProperty("notes", description)
            }
            if (!startDate.isNullOrEmpty()) {
                jsonObjectData.addProperty("due_on", startDate)
            }
            jsonObject.add("data", jsonObjectData)
            RetrofitUserAsanaClient.getAsanaUserClient(url = "https://app.asana.com/api/1.0/")
                .create(AccountIdService::class.java)
                .createAsanaTask(jsonObject = jsonObject)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        asanaExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        val coroutineCallAsanaAttachments = CoroutineScope(Dispatchers.IO)
                        val coroutineCallAsanSubtasks = CoroutineScope(Dispatchers.IO)
                        Log.d("asana_details", response.code().toString())
                        val asanaList = response.body()
                        if (asanaList != null) {
                            RecyclerViewAsanaAdapter.ViewHolder.arrayListFilePaths.forEach {
                                queueCounter++
                                coroutineCallAsanaAttachments.async {
                                    createAttachments(
                                        activity = activity,
                                        file = it.file,
                                        taskId = asanaList.asJsonObject["data"].asJsonObject["gid"].asString
                                    )
                                }
                            }
                            if (!section.isNullOrEmpty()) {
                                queueCounter++
                                val coroutineCallAsanaSection = CoroutineScope(Dispatchers.IO)
                                coroutineCallAsanaSection.async {
                                    asanaAddSection(
                                        activity = activity,
                                        sectionId = arrayListSectionsId[sectionPosition],
                                        taskId = asanaList.asJsonObject["data"].asJsonObject["gid"].asString
                                    )
                                }
                            }
                            if (RecyclerViewAsanaSubTaskAdapter.ViewHolder.arrayListSubtask.isNotEmpty()) {
                                RecyclerViewAsanaSubTaskAdapter.ViewHolder.arrayListSubtask.forEach {
                                    queueCounter++
                                    coroutineCallAsanSubtasks.async {
                                        asanaAddSubtask(
                                            activity = activity,
                                            taskId = asanaList.asJsonObject["data"].asJsonObject["gid"].asString,
                                            subtask = it.subtaskName
                                        )
                                    }
                                }
                            } else {
                                if (!subtask.isNullOrEmpty()) {
                                    queueCounter++
                                    coroutineCallAsanSubtasks.async {
                                        asanaAddSubtask(
                                            activity = activity,
                                            taskId = asanaList.asJsonObject["data"].asJsonObject["gid"].asString,
                                            subtask = subtask!!
                                        )
                                    }
                                }
                            }

                        }
                        resetasanaValues(shareLayoutMessage = "asana")
                    }
                })

        } catch (e: Exception) {
            asanaExceptionHandler(e = e)
        }
    }

    private fun asanaAddSection(activity: Activity, sectionId: String, taskId: String) {
        try {
            this.activity = activity
            val jsonObject = JsonObject()
            val jsonObjecTask = JsonObject()
            jsonObjecTask.addProperty("task", taskId)
            jsonObject.add("data", jsonObjecTask)
            RetrofitUserAsanaClient.getAsanaUserClient(url = "https://app.asana.com/api/1.0/sections/$sectionId/")
                .create(AccountIdService::class.java)
                .addAsanaSection(jsonObject = jsonObject)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        asanaExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        Log.d("asana_section_details", response.code().toString())
                        val asanaList = response.body()
                        resetasanaValues(shareLayoutMessage = "asana")
                    }
                })

        } catch (e: Exception) {
            asanaExceptionHandler(e = e)
        }
    }

    private fun asanaAddSubtask(activity: Activity, taskId: String, subtask: String) {
        try {
            this.activity = activity
            val jsonObject = JsonObject()
            val jsonObjectData = JsonObject()
            val jsonArrayProjects = JsonArray()
            jsonArrayProjects.add(projectId)
            jsonObjectData.add("projects", jsonArrayProjects)
            jsonObjectData.addProperty("name", subtask)
            if(!RecyclerViewAsanaSubTaskAdapter.ViewHolder.hashMapSubAssignee.isNullOrEmpty()){
                jsonObjectData.addProperty("assignee",arrayListAssigneeId[RecyclerViewAsanaSubTaskAdapter.ViewHolder.hashMapSubAssignee[subtask]!!] )

            }
            if (!RecyclerViewAsanaSubTaskAdapter.ViewHolder.hashMapSubDescription.isNullOrEmpty()) {
                jsonObjectData.addProperty("notes", RecyclerViewAsanaSubTaskAdapter.ViewHolder.hashMapSubDescription[subtask])
            }
            if (!RecyclerViewAsanaSubTaskAdapter.ViewHolder.hashMapSubDate.isNullOrEmpty()) {
                jsonObjectData.addProperty("due_on",RecyclerViewAsanaSubTaskAdapter.ViewHolder.hashMapSubDate[subtask])
            }
            jsonObject.add("data", jsonObjectData)
            RetrofitUserAsanaClient.getAsanaUserClient(url = "https://app.asana.com/api/1.0/tasks/$taskId/")
                .create(AccountIdService::class.java)
                .addAsanaSubtask(jsonObject = jsonObject)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        asanaExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        Log.d("asana_subtask_details", response.code().toString())
                        val coroutineCallAsanaSubAttachments = CoroutineScope(Dispatchers.IO)
                        val coroutineCallAsanaSubSection = CoroutineScope(Dispatchers.IO)
                        val asanaList = response.body()
                        if (asanaList != null) {
                            RecyclerViewAsanaSubTaskAdapter.ViewHolder.hashMapSubFile[subtask]?.forEach {
                                queueCounter++
                                coroutineCallAsanaSubAttachments.async {
                                    createAttachments(
                                        activity = activity,
                                        file = it.file,
                                        taskId = asanaList.asJsonObject["data"].asJsonObject["gid"].asString
                                    )
                                }
                            }
                            if (!RecyclerViewAsanaSubTaskAdapter.ViewHolder.hashmapSubSection.isNullOrEmpty()) {
                                    queueCounter++
                                    coroutineCallAsanaSubSection.async {
                                        asanaAddSection(
                                            activity = activity,
                                            sectionId = arrayListSectionsId[RecyclerViewAsanaSubTaskAdapter.ViewHolder.hashmapSubSection[subtask]!!],
                                            taskId = asanaList.asJsonObject["data"].asJsonObject["gid"].asString
                                        )
                                    }

                            }
                        }
                        resetasanaValues(shareLayoutMessage = "asana")
                    }
                })

        } catch (e: Exception) {
            asanaExceptionHandler(e = e)
        }
    }

    private fun gatherTaskProject() {
        queueCounter++
        RetrofitUserAsanaClient.getAsanaUserClient(url = "https://app.asana.com/api/1.0/")
            .create(AccountIdService::class.java)
            .getAsanaProject()
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    asanaExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    Log.d("asana_project_success", response.code().toString())
                    val asanaList = response.body()
                    val coroutineCallAsanaProject = CoroutineScope(Dispatchers.IO)
                    coroutineCallAsanaProject.async {
                        asanaList?.getAsJsonArray("data")?.forEach {
                            arrayListProjectNames.add(it.asJsonObject["name"].asString)
                            arrayListProjectId.add(it.asJsonObject["gid"].asString)
                        }
                        if (arrayListProjectId.size > projectPosition) {
                            gatherTaskAssignee(projectId = arrayListProjectId[projectPosition])
                            gatherTaskSections(projectId = arrayListProjectId[projectPosition])
                        }
                        updateFields()
                    }
                }
            })
    }


    private fun gatherTaskAssignee(projectId: String) {
        queueCounter++
        RetrofitUserAsanaClient.getAsanaUserClient(url = "https://app.asana.com/api/1.0/projects/$projectId/")
            .create(AccountIdService::class.java)
            .getAsanaAssignee()
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    asanaExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    val coroutineCallAsanaAssignee = CoroutineScope(Dispatchers.IO)
                    coroutineCallAsanaAssignee.async {
                        Log.d("asana_assignee_success", response.code().toString())
                        val asanaList = response.body()
                        asanaList?.getAsJsonArray("data")?.forEach {
                            arrayListAssigneeNames.add(it.asJsonObject["user"].asJsonObject["name"].asString)
                            arrayListAssigneeId.add(it.asJsonObject["user"].asJsonObject["gid"].asString)
                        }
                        updateFields()

                    }
                }
            })
    }

    private fun gatherTaskSections(projectId: String) {
        queueCounter++
        RetrofitUserAsanaClient.getAsanaUserClient(url = "https://app.asana.com/api/1.0/projects/$projectId/")
            .create(AccountIdService::class.java)
            .getAsanaSections()
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    asanaExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    val coroutineCallAsanaSections = CoroutineScope(Dispatchers.IO)
                    coroutineCallAsanaSections.async {
                        Log.d("asana_sections_success", response.code().toString())
                        val asanaList = response.body()
                        asanaList?.getAsJsonArray("data")?.forEach {
                            arrayListSectionsNames.add(it.asJsonObject["name"].asString)
                            arrayListSectionsId.add(it.asJsonObject["gid"].asString)
                        }
                        updateFields()

                    }
                }
            })
    }

    internal fun gatherAutoTextDetails(
        autoTextViewProject: AutoCompleteTextView,
        autoTextViewAssignee: AutoCompleteTextView,
        autoTextViewPriority: AutoCompleteTextView,
        autoTextViewSection: AutoCompleteTextView
    ) {
        project = autoTextViewProject.editableText.toString()
        assignee = autoTextViewAssignee.editableText.toString()
        priority = autoTextViewPriority.editableText.toString()
        section = autoTextViewSection.editableText.toString()
    }

    internal fun gatherEditTextDetails(
        editTextDescription: EditText,
        editTextSubtasks: EditText,
        editTextTaskName: EditText
    ) {
        description = editTextDescription.text.toString()
        subtask = editTextSubtasks.text.toString()
        taskName = editTextTaskName.text.toString()
    }

    private fun gatherAsanaDetails() {
        try {
            queueCounter = 0
            arrayListSectionsNames.clear()
            arrayListSectionsId.clear()
            arrayListPriorityNames.clear()
            arrayListAssigneeNames.clear()
            arrayListAssigneeId.clear()
            arrayListProjectNames.clear()
            arrayListProjectId.clear()
            gatherTaskProject()
        } catch (e: Exception) {
            asanaExceptionHandler(e = e)
        }
    }


    private fun updateFields() {
        queueCounter--
        Log.d("que_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeAsanaAutoTextViews(
                    arrayListProject = arrayListProjectNames,
                    arrayListAssignee = arrayListAssigneeNames,
                    arrayListSection = arrayListSectionsNames,
                    arrayListPriority = arrayListPriorityNames,
                    filePathMedia = filePathMedia!!
                )
            }
        }

    }


    private fun checkQueueTime(activity: Activity) {
        val timerQueue = Timer()
        timerTaskQueue = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread {
                    LoggerBirdService.loggerBirdService.finishShareLayout("asana_error_time_out")
                }
            }
        }
        timerQueue.schedule(timerTaskQueue, 180000)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun asanaExceptionHandler(
        e: Exception? = null,
        throwable: Throwable? = null
    ) {
        resetasanaValues(shareLayoutMessage = "asana_error")
        if (this::timerTaskQueue.isInitialized) {
            timerTaskQueue.cancel()
        }
        LoggerBirdService.loggerBirdService.finishShareLayout("asana_error")
        throwable?.printStackTrace()
        e?.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.asanaTag,
            throwable = throwable
        )
    }

    internal fun setProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }

    internal fun setAssignee(assigneePosition: Int) {
        this.assigneePosition = assigneePosition
    }

    internal fun setSectionPosition(sectionPosition: Int) {
        this.sectionPosition = sectionPosition
    }

    internal fun setStartDate(startDate: String?) {
        this.startDate = startDate
    }

    private fun resetasanaValues(shareLayoutMessage: String) {
        queueCounter--
        Log.d("queue_counter", queueCounter.toString())
        if (queueCounter == 0) {
            RecyclerViewAsanaAdapter.ViewHolder.arrayListFilePaths.forEach {
                if (it.file.name != "logger_bird_details.txt") {
                    if (it.file.exists()) {
                        it.file.delete()
                    }
                }
            }
            timerTaskQueue.cancel()
            arrayListProjectNames.clear()
            arrayListProjectId.clear()
            arrayListAssigneeNames.clear()
            arrayListAssigneeId.clear()
            arrayListPriorityNames.clear()
            arrayListSectionsNames.clear()
            arrayListSectionsId.clear()
            projectPosition = 0
            sectionPosition = 0
            project = null
            projectId = null
            startDate = null
            assignee = null
            priority = null
            section = null
            description = null
            subtask = null
            content = null
            taskName = null
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.finishShareLayout(shareLayoutMessage)
            }
        }
    }

    private fun createAttachments(
        taskId: String,
        file: File,
        activity: Activity
    ) {
        try {
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            RetrofitUserAsanaClient.getAsanaUserClient(url = "https://app.asana.com/api/1.0/tasks/$taskId/")
                .create(AccountIdService::class.java)
                .setAsanaAttachments(
                    file = body
                )
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        asanaExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        val coroutineCallAsanaAttachments = CoroutineScope(Dispatchers.IO)
                        coroutineCallAsanaAttachments.async {
                            val asanaResponse = response.body()
                            Log.d("attachment_put_success", response.code().toString())
                            Log.d("attachment_put_success", response.message())
                            resetasanaValues(shareLayoutMessage = "asana")
                        }
                    }
                })
        } catch (e: Exception) {
            asanaExceptionHandler(e = e)
        }
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkAsanaProject(
        activity: Activity,
        autoTextViewAsanaProject: AutoCompleteTextView
    ): Boolean {
        if (autoTextViewAsanaProject.editableText.toString().isNotEmpty() && arrayListProjectNames.contains(
                autoTextViewAsanaProject.editableText.toString()
            )
        ) {
            return true
        } else if (autoTextViewAsanaProject.editableText.toString().isEmpty()) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.asana_project_empty)
            )
        } else if (!arrayListProjectNames.contains(autoTextViewAsanaProject.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.asana_project_doesnt_exist)
            )
        }
        return false
    }

    internal fun checkAsanaTask(
        activity: Activity,
        editTextTask: EditText
    ): Boolean {
        if (editTextTask.text.toString().isNotEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.asana_task_empty)
            )
        }
        return false
    }

    internal fun checkAsanaSection(
        activity: Activity,
        autoTextViewAsanaSection: AutoCompleteTextView
    ): Boolean {
        if (arrayListSectionsNames.contains(autoTextViewAsanaSection.editableText.toString()) || autoTextViewAsanaSection.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.asana_section_doesnt_exist)
            )
        }
        return false
    }

    internal fun checkAsanaAssignee(
        activity: Activity,
        autoTextViewAsanaAssignee: AutoCompleteTextView
    ): Boolean {
        if (arrayListAssigneeNames.contains(autoTextViewAsanaAssignee.editableText.toString()) || autoTextViewAsanaAssignee.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.asana_assignee_doesnt_exist)
            )
        }
        return false
    }

}