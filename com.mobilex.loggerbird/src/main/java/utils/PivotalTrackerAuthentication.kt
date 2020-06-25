package utils

import adapter.RecyclerViewPivotalLabelAdapter
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

internal class PivotalTrackerAuthentication {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private var filePathMedia: File? = null
    private val coroutineCallOkHttpPivotal = CoroutineScope(Dispatchers.IO)
    private val coroutineCallPivotal = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private var queueCounter = 0
    private lateinit var timerTaskQueue: TimerTask
    private val arrayListProjectNames: ArrayList<String> = ArrayList()
    private val arrayListProjectId: ArrayList<String> = ArrayList()
    private val arrayListStoryTypeNames: ArrayList<String> = ArrayList()
    private val arrayListRequesterNames: ArrayList<String> = ArrayList()
    private val arrayListMemberId: ArrayList<String> = ArrayList()
    private val arrayListOwnersNames: ArrayList<String> = ArrayList()
    private val arrayListLabelId: ArrayList<String> = ArrayList()
    private val arrayListLabelNames: ArrayList<String> = ArrayList()
    private val arrayListPoints: ArrayList<String> = ArrayList()
    private val hashMapLabel: HashMap<String, String> = HashMap()
    private val hashMapMember: HashMap<String, String> = HashMap()
    private var projectPosition = 0
    private var labelPosition: Int = 0
    private var project: String? = null
    private var projectId:String? = null
    private var storyType: String? = null
    private var points: String? = null
    private var requester: String? = null
    private var tasks: String? = null
    private var description: String? = null
    private var blockers: String? = null
    private var title: String = ""
    private var label: String? = null
    private var owners: String? = null
    private val defaultToast = DefaultToast()
    internal fun callPivotal(
        activity: Activity,
        context: Context,
        task: String,
        filePathMedia: File? = null
    ) {
        this.activity = activity
        this.context = context
        this.filePathMedia = filePathMedia
        coroutineCallOkHttpPivotal.async {
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
                pivotalExceptionHandler(e = e)
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
            .url("https://pivotaltracker.com")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                pivotalExceptionHandler(e = e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("response_message", response.message)
                Log.d("response_code", response.code.toString())
                try {
                    if (response.code in 200..299) {
                        coroutineCallPivotal.async {
                            try {
                                when (task) {
                                    "create" -> pivotalCreateIssue(activity = activity)
                                    "get" -> gatherPivotalDetails()
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                LoggerBird.callEnqueue()
                                LoggerBird.callExceptionDetails(
                                    exception = e,
                                    tag = Constants.trelloTag
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
                    pivotalExceptionHandler(e = e)
                }
            }
        })
    }

    private fun pivotalCreateIssue(activity: Activity) {
        try {
            projectId = arrayListProjectId[projectPosition]
            queueCounter = 0
            queueCounter++
            this.activity = activity
            val jsonObject = JsonObject()
//            val jsonArrayLabels = JsonArray()
//            if (RecyclerViewTrelloLabelAdapter.ViewHolder.arrayListLabelNames.isNotEmpty()) {
//                RecyclerViewTrelloLabelAdapter.ViewHolder.arrayListLabelNames.forEach {
//                    jsonArrayLabels.add(hashMapLabel[it.labelName])
//                }
//            } else {
//                if (!label.isNullOrEmpty()) {
//                    jsonArrayLabels.add(hashMapLabel[label!!])
//                }
//            }
//            val jsonArrayMembers = JsonArray()
//            if(RecyclerViewTrelloMemberAdapter.ViewHolder.arrayListMemberNames.isNotEmpty()){
//                RecyclerViewTrelloMemberAdapter.ViewHolder.arrayListMemberNames.forEach {
//                    jsonArrayMembers.add(hashMapMember[it.memberName])
//                }
//            }else{
//                if(!member.isNullOrEmpty())
//                {
//                jsonArrayMembers.add(hashMapMember[member!!])
//                }
//            }
//            jsonObject.add("idMembers",jsonArrayMembers)
//            jsonObject.add("idLabels",jsonArrayLabels)
            val jsonArrayLabels = JsonArray()
            if(RecyclerViewPivotalLabelAdapter.ViewHolder.arrayListLabelNames.isNotEmpty()){
                RecyclerViewPivotalLabelAdapter.ViewHolder.arrayListLabelNames.forEach {
                    jsonArrayLabels.add(hashMapLabel[it.labelName]?.toInt())
                }
            }else{
                if(!label.isNullOrEmpty()){
                    jsonArrayLabels.add(label)
                }
            }
            if(!storyType.isNullOrEmpty()){
                jsonObject.addProperty("story_type",storyType)
            }
            jsonObject.add("label_ids",jsonArrayLabels)
            jsonObject.addProperty("name", title)
            RetrofitUserPivotalClient.getPivotalUserClient(url = "https://www.pivotaltracker.com/services/v5/projects/$projectId/")
                .create(AccountIdService::class.java)
                .createPivotalStory(jsonObject = jsonObject)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        pivotalExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
//                        val coroutineCallTrelloAttachments = CoroutineScope(Dispatchers.IO)
                        Log.d("pivotal_details", response.code().toString())
                        val pivotalList = response.body()
//                        RecyclerViewTrelloAdapter.ViewHolder.arrayListFilePaths.forEach {
//                            queueCounter++
//                            coroutineCallTrelloAttachments.async {
//                                createAttachments(
//                                    activity = activity,
//                                    file = it.file,
//                                    cardId = trelloList!!["id"].asString
//                                )
//                            }
////                            repoId = githubList!!["url"].asString.substringAfterLast("/").toInt()
////                            RecyclerViewGithubAdapter.ViewHolder.arrayListFilePaths.forEach {
////                                val file = it.file
////                                if (file.exists()) {
////                                    callGithubAttachments(
////                                        repo = repos!!,
////                                        filePathMedia = file
////                                    )
////                                }
////                            }
////                            if (RecyclerViewGithubAdapter.ViewHolder.arrayListFilePaths.isEmpty()) {
////                                activity.runOnUiThread {
////                                    LoggerBirdService.loggerBirdService.finishShareLayout("github")
////                                }
////                            }
//                        }
                        resetPivotalValues()
                    }
                })

        } catch (e: Exception) {
            pivotalExceptionHandler(e = e)
        }
    }

    private fun gatherTaskProject() {
        queueCounter++
        RetrofitUserPivotalClient.getPivotalUserClient(url = "https://pivotaltracker.com/services/v5/")
            .create(AccountIdService::class.java)
            .getPivotalProjects()
            .enqueue(object : retrofit2.Callback<JsonArray> {
                override fun onFailure(
                    call: retrofit2.Call<JsonArray>,
                    t: Throwable
                ) {
                    pivotalExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonArray>,
                    response: retrofit2.Response<JsonArray>
                ) {
                    val coroutineCallPivotalProject = CoroutineScope(Dispatchers.IO)
                    coroutineCallPivotalProject.async {
                        Log.d("pivotal_project_success", response.code().toString())
                        val pivotalList = response.body()
                        pivotalList?.forEach {
                            if (it.asJsonObject["name"] != null) {
                                arrayListProjectNames.add(it.asJsonObject["name"].asString)
                                arrayListProjectId.add(it.asJsonObject["id"].asString)
                            }
                        }
                        if (arrayListProjectId.size > projectPosition) {
                            gatherTaskLabel(projectId = arrayListProjectId[projectPosition])
                            gatherTaskMembers(projectId = arrayListProjectId[projectPosition])
                        }
                        updateFields()

                    }
                }
            })
    }

    private fun gatherTaskStoryType() {
        arrayListStoryTypeNames.add("feature")
        arrayListStoryTypeNames.add("bug")
        arrayListStoryTypeNames.add("chore")
        arrayListStoryTypeNames.add("release")
    }

    private fun gatherTaskPoints() {
        arrayListPoints.add("0")
        arrayListPoints.add("1")
        arrayListPoints.add("2")
        arrayListPoints.add("3")
    }

    private fun gatherTaskLabel(projectId: String) {
        queueCounter++
        RetrofitUserPivotalClient.getPivotalUserClient(url = "https://pivotaltracker.com/services/v5/projects/$projectId/")
            .create(AccountIdService::class.java)
            .getPivotalLabels()
            .enqueue(object : retrofit2.Callback<JsonArray> {
                override fun onFailure(
                    call: retrofit2.Call<JsonArray>,
                    t: Throwable
                ) {
                    pivotalExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonArray>,
                    response: retrofit2.Response<JsonArray>
                ) {
                    val coroutineCallLabel = CoroutineScope(Dispatchers.IO)
                    coroutineCallLabel.async {
                        try {
                            Log.d("pivotal_label_success", response.code().toString())
                            val labelList = response.body()
                            labelList?.forEach {
                                //                                if (it.asJsonObject["name"].asString.isNotEmpty() && it.asJsonObject["name"].asString != null ) {
//                                    arrayListOwnersNames.add(it.asJsonObject["name"].asString)
//                                    hashMapLabel[it.asJsonObject["name"].asString] = it.asJsonObject["id"].asString
//                                } else {
//                                    arrayListOwnersNames.add(it.asJsonObject["id"].asString)
//                                    hashMapLabel[it.asJsonObject["id"].asString] = it.asJsonObject["id"].asString
//                                }
                                arrayListLabelId.add(it.asJsonObject["id"].asString)
                                arrayListLabelNames.add(it.asJsonObject["name"].asString)
                                hashMapLabel[it.asJsonObject["name"].asString] = it.asJsonObject["id"].asString
                            }
                            updateFields()
                        } catch (e: Exception) {
                            pivotalExceptionHandler(e = e)
                        }
                    }
                }
            })
    }

    private fun gatherTaskMembers(projectId: String) {
        queueCounter++
        RetrofitUserPivotalClient.getPivotalUserClient(url = "https://pivotaltracker.com/services/v5/projects/$projectId/")
            .create(AccountIdService::class.java)
            .getPivotalMembers()
            .enqueue(object : retrofit2.Callback<JsonArray> {
                override fun onFailure(
                    call: retrofit2.Call<JsonArray>,
                    t: Throwable
                ) {
                    pivotalExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonArray>,
                    response: retrofit2.Response<JsonArray>
                ) {
                    val coroutineCallLabel = CoroutineScope(Dispatchers.IO)
                    coroutineCallLabel.async {
                        try {
                            Log.d("pivotal_member_success", response.code().toString())
                            val memberList = response.body()
                            memberList?.forEach {
                                arrayListMemberId.add(it.asJsonObject["person"].asJsonObject["id"].asString)
                                arrayListRequesterNames.add(it.asJsonObject["person"].asJsonObject["name"].asString)
                                arrayListOwnersNames.add(it.asJsonObject["person"].asJsonObject["name"].asString)
                            }
                            updateFields()
                        } catch (e: Exception) {
                            pivotalExceptionHandler(e = e)
                        }
                    }
                }
            })
    }


    internal fun gatherAutoTextDetails(
        autoTextViewProject: AutoCompleteTextView,
        autoTextViewStoryType: AutoCompleteTextView,
        autoTextViewPoints: AutoCompleteTextView,
        autoTextViewRequester: AutoCompleteTextView,
        autoTextViewLabel: AutoCompleteTextView,
        autoTextViewOwners: AutoCompleteTextView
    ) {
        project = autoTextViewProject.editableText.toString()
        storyType = autoTextViewStoryType.editableText.toString()
        points = autoTextViewPoints.editableText.toString()
        requester = autoTextViewRequester.editableText.toString()
        label = autoTextViewLabel.editableText.toString()
        owners = autoTextViewOwners.editableText.toString()
    }

    internal fun gatherEditTextDetails(
        editTextTitle: EditText,
        editTextTasks: EditText,
        editTextDescription: EditText,
        editTextBlockers: EditText
    ) {
        title = editTextTitle.text.toString()
        tasks = editTextTasks.text.toString()
        description = editTextDescription.text.toString()
        blockers = editTextBlockers.text.toString()
    }

    private fun gatherPivotalDetails() {
        try {
            queueCounter = 0
            arrayListProjectNames.clear()
            arrayListProjectId.clear()
            arrayListStoryTypeNames.clear()
            arrayListRequesterNames.clear()
            arrayListMemberId.clear()
            arrayListOwnersNames.clear()
            arrayListLabelId.clear()
            arrayListLabelNames.clear()
            hashMapLabel.clear()
            arrayListPoints.clear()
            gatherTaskProject()
            gatherTaskStoryType()
            gatherTaskPoints()
        } catch (e: Exception) {
            pivotalExceptionHandler(e = e)
        }
    }


    private fun updateFields() {
        queueCounter--
        Log.d("que_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializePivotalAutoTextViews(
                    arrayListProject = arrayListProjectNames,
                    arrayListStoryType = arrayListStoryTypeNames,
                    arrayListRequester = arrayListRequesterNames,
                    arrayListOwners = arrayListOwnersNames,
                    arrayListLabels = arrayListLabelNames,
                    arrayListPoints = arrayListPoints
                )
            }
        }

    }


    private fun checkQueueTime(activity: Activity) {
        val timerQueue = Timer()
        timerTaskQueue = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread {
                    LoggerBirdService.loggerBirdService.finishShareLayout("pivotal_error_time_out")
                }
            }
        }
        timerQueue.schedule(timerTaskQueue, 180000)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun pivotalExceptionHandler(
        e: Exception? = null,
        throwable: Throwable? = null
    ) {
        resetPivotalValues()
        if (this::timerTaskQueue.isInitialized) {
            timerTaskQueue.cancel()
        }
        LoggerBirdService.loggerBirdService.finishShareLayout("pivotal_error")
        throwable?.printStackTrace()
        e?.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.pivotalTag,
            throwable = throwable
        )
    }

    internal fun setProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }

    internal fun setLabelPosition(labelPosition: Int) {
        this.labelPosition = labelPosition
    }


    private fun resetPivotalValues() {
        queueCounter--
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            arrayListProjectNames.clear()
            arrayListProjectId.clear()
            arrayListStoryTypeNames.clear()
            arrayListOwnersNames.clear()
            hashMapLabel.clear()
            hashMapMember.clear()
            arrayListLabelId.clear()
            arrayListLabelNames.clear()
            hashMapLabel.clear()
            projectPosition = 0
            project = null
            title = ""
            label = null
            labelPosition = 0
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.finishShareLayout("pivotal")
            }
        }
    }

    private fun createAttachments(cardId: String, file: File, activity: Activity) {
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        RetrofitUserTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/cards/$cardId/")
            .create(AccountIdService::class.java)
            .setTrelloAttachments(
                file = body,
                key = LoggerBird.trelloKey,
                token = LoggerBird.trelloToken
            )
            .enqueue(object : retrofit2.Callback<JsonObject> {
                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    resetPivotalValues()
                    pivotalExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    val coroutineCallTrelloAttachments = CoroutineScope(Dispatchers.IO)
                    coroutineCallTrelloAttachments.async {
                        if (file.name != "logger_bird_details.txt") {
                            if (file.exists()) {
                                file.delete()
                            }
                        }
                        Log.d("attachment_put_success", response.code().toString())
                        Log.d("attachment_put_success", response.message())
                        resetPivotalValues()
                    }
                }
            })
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkPivotalProject(
        activity: Activity,
        autoTextViewPivotalProject: AutoCompleteTextView
    ): Boolean {
        if (autoTextViewPivotalProject.editableText.toString().isNotEmpty() && arrayListProjectNames.contains(
                autoTextViewPivotalProject.editableText.toString()
            )
        ) {
            return true
        } else if (autoTextViewPivotalProject.editableText.toString().isEmpty()) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_project_empty)
            )
        } else if (!arrayListProjectNames.contains(autoTextViewPivotalProject.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_project_doesnt_exist)
            )
        }
        return false
    }

    internal fun checkPivotalLabel(
        activity: Activity,
        autoTextViewPivotalLabel: AutoCompleteTextView
    ): Boolean {
        if (arrayListLabelNames.contains(autoTextViewPivotalLabel.editableText.toString()) || autoTextViewPivotalLabel.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_label_doesnt_exist)
            )
        }
        return false
    }

    internal fun checkPivotalRequester(
        activity: Activity,
        autoTextViewPivotaRequester: AutoCompleteTextView
    ): Boolean {
        if (arrayListRequesterNames.contains(autoTextViewPivotaRequester.editableText.toString()) || autoTextViewPivotaRequester.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_requester_doesnt_exist)
            )
        }
        return false
    }

    internal fun checkPivotalOwner(
        activity: Activity,
        autoTextViewPivotalOwner: AutoCompleteTextView
    ): Boolean {
        if (arrayListOwnersNames.contains(autoTextViewPivotalOwner.editableText.toString()) || autoTextViewPivotalOwner.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_owner_doesnt_exist)
            )
        }
        return false
    }
    internal fun checkPivotalStoryType(
        activity: Activity,
        autoTextViewPivotalStoryType: AutoCompleteTextView
    ): Boolean {
        if (arrayListStoryTypeNames.contains(autoTextViewPivotalStoryType.editableText.toString()) || autoTextViewPivotalStoryType.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_story_type_doesnt_exist)
            )
        }
        return false
    }
    internal fun checkPivotalPoint(
        activity: Activity,
        autoTextViewPivotalPoint: AutoCompleteTextView
    ): Boolean {
        if (arrayListPoints.contains(autoTextViewPivotalPoint.editableText.toString()) || autoTextViewPivotalPoint.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_points_doesnt_exist)
            )
        }
        return false
    }

    internal fun checkPivotalTitle(activity: Activity, editTextTitle: EditText): Boolean {
        if (editTextTitle.text.toString().isNotEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_project_empty)
            )
        }
        return false
    }
}