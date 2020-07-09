package utils.api.pivotal

import adapter.recyclerView.api.pivotal.*
import adapter.recyclerView.api.pivotal.RecyclerViewPivotalAttachmentAdapter
import adapter.recyclerView.api.pivotal.RecyclerViewPivotalBlockerAdapter
import adapter.recyclerView.api.pivotal.RecyclerViewPivotalLabelAdapter
import adapter.recyclerView.api.pivotal.RecyclerViewPivotalOwnerAdapter
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

/** Loggerbird PivotalTracker api configration class **/
internal class PivotalTrackerApi {
    //Global variables.
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
    private val hashMapOwner: HashMap<String, String> = HashMap()
    private var projectPosition = 0
    private var labelPosition: Int = 0
    private var project: String? = null
    private var projectId: String? = null
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
    /**
     * This method is used for calling an pivotal action with network connection check.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param task is for getting reference of which pivotal action will be executed.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws LoggerBirdException if network connection error occurs.
     * @throws exception if error occurs.
     * @see pivotalExceptionHandler method.
     */
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

    /**
     * This method is used for calling an pivotal action with internet connection check.
     * @param context is for getting reference from the application context.
     * @param activity is used for getting reference of current activity.
     * @param task is for getting reference of which pivotal action will be executed.
     * @param filePathMediaName is used for getting the reference of current media file.
     * @throws LoggerBirdException if internet connection error occurs.
     * @throws exception if error occurs.
     * @see pivotalExceptionHandler method.
     */
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
                                    "create" -> pivotalCreateStory(activity = activity)
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

    /**
     * This method is used for creating pivotal story.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs.
     * @see pivotalExceptionHandler method.
     */
    private fun pivotalCreateStory(activity: Activity) {
        try {
            projectId = arrayListProjectId[projectPosition]
            queueCounter = 0
            queueCounter++
            this.activity = activity
            val jsonObject = JsonObject()
            val jsonArrayLabels = JsonArray()
            if (RecyclerViewPivotalLabelAdapter.ViewHolder.arrayListLabelNames.isNotEmpty()) {
                RecyclerViewPivotalLabelAdapter.ViewHolder.arrayListLabelNames.forEach {
                    jsonArrayLabels.add(hashMapLabel[it.labelName]?.toInt())
                }
                jsonObject.add("label_ids", jsonArrayLabels)
            } else {
                if (!label.isNullOrEmpty()) {
                    jsonArrayLabels.add(hashMapLabel[label!!]?.toInt())
                    jsonObject.add("label_ids", jsonArrayLabels)
                }
            }
            val jsonArrayOwners = JsonArray()
            if (RecyclerViewPivotalOwnerAdapter.ViewHolder.arrayListOwnerNames.isNotEmpty()) {
                RecyclerViewPivotalOwnerAdapter.ViewHolder.arrayListOwnerNames.forEach {
                    jsonArrayOwners.add(hashMapOwner[it.ownerName]?.toInt())
                }
                jsonObject.add("owner_ids", jsonArrayOwners)
            } else {
                if (!owners.isNullOrEmpty()) {
                    jsonArrayOwners.add(hashMapOwner[owners!!]?.toInt())
                    jsonObject.add("owner_ids", jsonArrayOwners)
                }
            }
            if (!points.isNullOrEmpty()) {
                jsonObject.addProperty("estimate", points!!.toFloat())
            }
            if (!requester.isNullOrEmpty()) {
                jsonObject.addProperty("requested_by_id", hashMapOwner[requester!!]?.toInt())
            }
            if (!storyType.isNullOrEmpty()) {
                jsonObject.addProperty("story_type", storyType)
            }
            if (!description.isNullOrEmpty()) {
                jsonObject.addProperty("description", description)
            }
            jsonObject.addProperty("name", title)
            RetrofitPivotalClient.getPivotalUserClient(url = "https://www.pivotaltracker.com/services/v5/projects/$projectId/")
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
                        val coroutineCallPivotalAttachments = CoroutineScope(Dispatchers.IO)
                        val coroutineCallPivotalBlockers = CoroutineScope(Dispatchers.IO)
                        val coroutineCallPivotalTasks = CoroutineScope(Dispatchers.IO)
                        Log.d("pivotal_details", response.code().toString())
                        val pivotalList = response.body()
                        RecyclerViewPivotalAttachmentAdapter.ViewHolder.arrayListFilePaths.forEach {
                            queueCounter++
                            coroutineCallPivotalAttachments.async {
                                createAttachments(
                                    activity = activity,
                                    file = it.file,
                                    projectId = projectId!!,
                                    storyId = pivotalList!!["id"].asString
                                )
                            }
                        }
                        if (RecyclerViewPivotalBlockerAdapter.ViewHolder.arrayListBlocker.isNotEmpty()) {
                            RecyclerViewPivotalBlockerAdapter.ViewHolder.arrayListBlocker.forEach {
                                queueCounter++
                                coroutineCallPivotalBlockers.async {
                                    createBlockers(
                                        activity = activity,
                                        projectId = projectId!!,
                                        storyId = pivotalList!!["id"].asString,
                                        description = it.blockerName
                                    )
                                }
                            }
                        } else {
                            if (!blockers.isNullOrEmpty()) {
                                queueCounter++
                                coroutineCallPivotalBlockers.async {
                                    createBlockers(
                                        activity = activity,
                                        projectId = projectId!!,
                                        storyId = pivotalList!!["id"].asString,
                                        description = blockers!!
                                    )
                                }
                            }
                        }

                        if (RecyclerViewPivotalTaskAdapter.ViewHolder.arrayListTasks.isNotEmpty()) {
                            RecyclerViewPivotalTaskAdapter.ViewHolder.arrayListTasks.forEach {
                                queueCounter++
                                coroutineCallPivotalTasks.async {
                                    createTasks(
                                        activity = activity,
                                        projectId = projectId!!,
                                        storyId = pivotalList!!["id"].asString,
                                        description = it.taskName
                                    )
                                }
                            }
                        } else {
                            if (!tasks.isNullOrEmpty()) {
                                queueCounter++
                                coroutineCallPivotalTasks.async {
                                    createTasks(
                                        activity = activity,
                                        projectId = projectId!!,
                                        storyId = pivotalList!!["id"].asString,
                                        description = tasks!!
                                    )
                                }
                            }
                        }

                        resetPivotalValues(shareLayoutMessage = "pivotal")
                    }
                })

        } catch (e: Exception) {
            pivotalExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for creating pivotal blockers when pivotal story created.
     * @param projectId is used for getting reference of current chosen project id.
     * @param storyId is used for getting reference of created story id.
     * @param description is used for getting reference of the description.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs.
     * @see pivotalExceptionHandler method.
     */
    private fun createBlockers(
        projectId: String,
        storyId: String,
        description: String,
        activity: Activity
    ) {
        try {
            val jsonObject = JsonObject()
            jsonObject.addProperty("description", description)
            RetrofitPivotalClient.getPivotalUserClient(url = "https://www.pivotaltracker.com/services/v5/projects/$projectId/stories/$storyId/")
                .create(AccountIdService::class.java)
                .setPivotalBlockers(jsonObject = jsonObject)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
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
                        val pivotalResponse = response.body()
                        Log.d("blockers_put_success", response.code().toString())
                        Log.d("blockers_put_success", response.message())
                        if (pivotalResponse != null) {
                            resetPivotalValues(shareLayoutMessage = "pivotal")
                        } else {
                            resetPivotalValues(shareLayoutMessage = "pivotal_error")
                        }

                    }
                })
        } catch (e: Exception) {
            pivotalExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for creating pivotal task when pivotal story created.
     * @param projectId is used for getting reference of current chosen project id.
     * @param storyId is used for getting reference of created story id.
     * @param description is used for getting reference of the description.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs.
     * @see pivotalExceptionHandler method.
     */
    private fun createTasks(
        projectId: String,
        storyId: String,
        description: String,
        activity: Activity
    ) {
        try {
            val jsonObject = JsonObject()
            jsonObject.addProperty("description", description)
            RetrofitPivotalClient.getPivotalUserClient(url = "https://www.pivotaltracker.com/services/v5/projects/$projectId/stories/$storyId/")
                .create(AccountIdService::class.java)
                .setPivotalTasks(jsonObject = jsonObject)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
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
                        val pivotalResponse = response.body()
                        Log.d("tasks_put_success", response.code().toString())
                        Log.d("tasks_put_success", response.message())
                        if (pivotalResponse != null) {
                            resetPivotalValues(shareLayoutMessage = "pivotal")
                        } else {
                            resetPivotalValues(shareLayoutMessage = "pivotal_error")
                        }

                    }
                })
        } catch (e: Exception) {
            pivotalExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for creating pivotal attachment when pivotal story created.
     * @param projectId is used for getting reference of current chosen project id.
     * @param storyId is used for getting reference of created story id.
     * @param file is used for getting reference of the current file.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs.
     * @see pivotalExceptionHandler method.
     */
    private fun createAttachments(
        projectId: String,
        storyId: String,
        file: File,
        activity: Activity
    ) {
        try {
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            RetrofitPivotalClient.getPivotalUserClient(url = "https://www.pivotaltracker.com/services/v5/projects/$projectId/")
                .create(AccountIdService::class.java)
                .setPivotalAttachments(
                    file = body
                )
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
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
                        val coroutineCallPivotalAttachments = CoroutineScope(Dispatchers.IO)
                        coroutineCallPivotalAttachments.async {
                            if (file.name != "logger_bird_details.txt") {
                                if (file.exists()) {
                                    file.delete()
                                }
                            }
                            val pivotalResponse = response.body()
                            Log.d("attachment_put_success", response.code().toString())
                            Log.d("attachment_put_success", response.message())
                            if (pivotalResponse != null) {
                                val coroutineCallPivotalAddAttachments =
                                    CoroutineScope(Dispatchers.IO)
                                coroutineCallPivotalAddAttachments.async {
                                    addAttachments(
                                        activity = activity,
                                        projectId = projectId,
                                        storyId = storyId,
                                        attachmentId = pivotalResponse.getAsJsonPrimitive("id").asString
                                    )
                                }
                            } else {
                                resetPivotalValues(shareLayoutMessage = "pivotal_error")
                            }

                        }
                    }
                })
        } catch (e: Exception) {
            pivotalExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for adding pivotal attachment when pivotal attachment created.
     * @param activity is used for getting reference of current activity.
     * @param projectId is used for getting reference of current chosen project id.
     * @param storyId is used for getting reference of created story id.
     * @param attachmentId is used for getting reference of created attachment id.
     * @throws exception if error occurs.
     * @see pivotalExceptionHandler method.
     */
    private fun addAttachments(
        activity: Activity,
        projectId: String,
        storyId: String,
        attachmentId: String
    ) {
        try {
            val jsonObject = JsonObject()
            val jsonArray = JsonArray()
            val jsonObjectId = JsonObject()
            jsonObjectId.addProperty("id", attachmentId)
            jsonArray.add(jsonObjectId)
            jsonObject.add("file_attachments", jsonArray)
            RetrofitPivotalClient.getPivotalUserClient(url = "https://www.pivotaltracker.com/services/v5/projects/$projectId/stories/$storyId/")
                .create(AccountIdService::class.java)
                .addPivotalAttachments(jsonObject = jsonObject)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        resetPivotalValues(shareLayoutMessage = "pivotal_error")
                        pivotalExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        Log.d("attachment_add_sucess", response.code().toString())
                        Log.d("attachment_add_sucess", response.message())
                        resetPivotalValues(shareLayoutMessage = "pivotal")
                    }
                })
        } catch (e: Exception) {
            pivotalExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for initializing the gathering action of pivotal.
     * @throws exception if error occurs.
     * @see pivotalExceptionHandler method.
     */
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
            hashMapOwner.clear()
            arrayListPoints.clear()
            gatherTaskProject()
            gatherTaskStoryType()
            gatherTaskPoints()
        } catch (e: Exception) {
            pivotalExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for getting project details for pivotal.
     * @throws exception if error occurs.
     * @see pivotalExceptionHandler method.
     */
    private fun gatherTaskProject() {
        queueCounter++
        RetrofitPivotalClient.getPivotalUserClient(url = "https://pivotaltracker.com/services/v5/")
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

    /**
     * This method is used for getting story type details for github.
     */
    private fun gatherTaskStoryType() {
        arrayListStoryTypeNames.add("feature")
        arrayListStoryTypeNames.add("bug")
        arrayListStoryTypeNames.add("chore")
        arrayListStoryTypeNames.add("release")
    }

    /**
     * This method is used for getting point details for github.
     */
    private fun gatherTaskPoints() {
        arrayListPoints.add("0")
        arrayListPoints.add("1")
        arrayListPoints.add("2")
        arrayListPoints.add("3")
    }


    /**
     * This method is used for getting label details for pivotal.
     * @param projectId is used for getting reference of current chosen project id.
     * @throws exception if error occurs.
     * @see pivotalExceptionHandler method.
     */
    private fun gatherTaskLabel(projectId: String) {
        queueCounter++
        RetrofitPivotalClient.getPivotalUserClient(url = "https://pivotaltracker.com/services/v5/projects/$projectId/")
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
                                arrayListLabelId.add(it.asJsonObject["id"].asString)
                                arrayListLabelNames.add(it.asJsonObject["name"].asString)
                                hashMapLabel[it.asJsonObject["name"].asString] =
                                    it.asJsonObject["id"].asString
                            }
                            updateFields()
                        } catch (e: Exception) {
                            pivotalExceptionHandler(e = e)
                        }
                    }
                }
            })
    }

    /**
     * This method is used for getting member details for pivotal.
     * @param projectId is used for getting reference of current chosen project id.
     * @throws exception if error occurs.
     * @see pivotalExceptionHandler method.
     */
    private fun gatherTaskMembers(projectId: String) {
        queueCounter++
        RetrofitPivotalClient.getPivotalUserClient(url = "https://pivotaltracker.com/services/v5/projects/$projectId/")
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
                                hashMapOwner[it.asJsonObject["person"].asJsonObject["name"].asString] =
                                    it.asJsonObject["person"].asJsonObject["id"].asString
                            }
                            updateFields()
                        } catch (e: Exception) {
                            pivotalExceptionHandler(e = e)
                        }
                    }
                }
            })
    }


    /**
     * This method is used for getting details of autoCompleteTextViews in the pivotal layout.
     * @param autoTextViewProject is used for getting project details from project autoCompleteTextView in the pivotal layout.
     * @param autoTextViewStoryType is used for getting story type details from story type autoCompleteTextView in the pivotal layout.
     * @param autoTextViewPoints is used for getting point details from point autoCompleteTextView in the pivotal layout.
     * @param autoTextViewRequester is used for getting requester details from requester autoCompleteTextView in the pivotal layout.
     * @param autoTextViewLabel is used for getting label details from label autoCompleteTextView in the pivotal layout.
     * @param autoTextViewOwners is used for getting owner details from owner autoCompleteTextView in the pivotal layout.
     */
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

    /**
     * This method is used for getting details of editTexts in the pivotal layout.
     * @param editTextTitle is used for getting title details from title editText in the pivotal layout.
     * @param editTextTasks is used for getting task details from task editText in the pivotal layout.
     * @param editTextDescription is used for getting description details from description editText in the pivotal layout.
     * @param editTextBlockers is used for getting blocker details from blocker editText in the pivotal layout.
     */
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


    /**
     * This method is used for updating and controlling the queue of background tasks in the pivotal actions.
     */
    private fun updateFields() {
        queueCounter--
        Log.d("que_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializePivotalAutoTextViews(
                    arrayListPivotalProject = arrayListProjectNames,
                    arrayListPivotalStoryType = arrayListStoryTypeNames,
                    arrayListPivotalRequester = arrayListRequesterNames,
                    arrayListPivotalOwners = arrayListOwnersNames,
                    arrayListPivotalLabels = arrayListLabelNames,
                    arrayListPivotalPoints = arrayListPoints
                )
            }
        }

    }

    /**
     * This method is used for controlling the time of background tasks in the pivotal actions.
    If tasks will last longer than three minutes then pivotal layout will be removed.
     */
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

    /**
     * This method is used for default exception handling of pivotal class.
     * @param e is used for getting reference of exception.
     * @param throwable is used for getting reference of throwable.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun pivotalExceptionHandler(
        e: Exception? = null,
        throwable: Throwable? = null
    ) {
        resetPivotalValues(shareLayoutMessage = "pivotal_error")
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

    /**
     * This method is used for getting reference of current project position in the project autoCompleteTextView in the pivotal layout.
     * @param projectPosition is used for getting reference of project position.
     */
    internal fun setProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }

    /**
     * This method is used for getting reference of current label position in the label autoCompleteTextView in the pivotal layout.
     * @param labelPosition is used for getting reference of label position.
     */
    internal fun setLabelPosition(labelPosition: Int) {
        this.labelPosition = labelPosition
    }


    /**
     * This method is used for resetting the values in pivotal action.
     * @param shareLayoutMessage is used for getting reference of the pivotal action message.
     */
    private fun resetPivotalValues(shareLayoutMessage: String) {
        queueCounter--
        Log.d("queue_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            arrayListProjectNames.clear()
            arrayListProjectId.clear()
            arrayListStoryTypeNames.clear()
            arrayListOwnersNames.clear()
            hashMapLabel.clear()
            hashMapOwner.clear()
            arrayListLabelId.clear()
            arrayListLabelNames.clear()
            projectPosition = 0
            project = null
            title = ""
            label = null
            labelPosition = 0
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.finishShareLayout(shareLayoutMessage)
            }
        }
    }

    /**
     * This method is used for checking project reference exist in the project list or not empty in the project AutoCompleteTextView field in the pivotal layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewProject is used for getting reference of project autoCompleteTextView in the pivotal layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkPivotalProject(
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
                toastMessage = activity.resources.getString(R.string.pivotal_project_empty)
            )
        } else if (!arrayListProjectNames.contains(autoTextViewProject.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_project_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking label reference exist in the label list or not empty in the label AutoCompleteTextView field in the pivotal layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewLabel is used for getting reference of label autoCompleteTextView in the pivotal layout.
     * @return Boolean value.
     */
    internal fun checkPivotalLabel(
        activity: Activity,
        autoTextViewLabel: AutoCompleteTextView
    ): Boolean {
        if (arrayListLabelNames.contains(autoTextViewLabel.editableText.toString()) || autoTextViewLabel.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_label_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking requester reference exist in the requester list or not empty in the requester AutoCompleteTextView field in the pivotal layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewRequester is used for getting reference of requester autoCompleteTextView in the pivotal layout.
     * @return Boolean value.
     */
    internal fun checkPivotalRequester(
        activity: Activity,
        autoTextViewRequester: AutoCompleteTextView
    ): Boolean {
        if (arrayListRequesterNames.contains(autoTextViewRequester.editableText.toString()) || autoTextViewRequester.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_requester_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking owner reference exist in the owner list or not empty in the owner AutoCompleteTextView field in the pivotal layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewOwner is used for getting reference of owner autoCompleteTextView in the pivotal layout.
     * @return Boolean value.
     */
    internal fun checkPivotalOwner(
        activity: Activity,
        autoTextViewOwner: AutoCompleteTextView
    ): Boolean {
        if (arrayListOwnersNames.contains(autoTextViewOwner.editableText.toString()) || autoTextViewOwner.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_owner_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking story type reference exist in the story type list or not empty in the story type AutoCompleteTextView field in the pivotal layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewStoryType is used for getting reference of story type autoCompleteTextView in the pivotal layout.
     * @return Boolean value.
     */
    internal fun checkPivotalStoryType(
        activity: Activity,
        autoTextViewStoryType: AutoCompleteTextView
    ): Boolean {
        if (arrayListStoryTypeNames.contains(autoTextViewStoryType.editableText.toString()) || autoTextViewStoryType.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_story_type_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking point reference exist in the point list or not empty in the point AutoCompleteTextView field in the pivotal layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewPoint is used for getting reference of point autoCompleteTextView in the pivotal layout.
     * @return Boolean value.
     */
    internal fun checkPivotalPoint(
        activity: Activity,
        autoTextViewPoint: AutoCompleteTextView
    ): Boolean {
        if (arrayListPoints.contains(autoTextViewPoint.editableText.toString()) || autoTextViewPoint.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_points_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking title reference is not empty in the task editText field in the pivotal layout.
     * @param activity is used for getting reference of current activity.
     * @param editTextTitle is used for getting reference of task editText in the pivotal layout.
     * @return Boolean value.
     */
    internal fun checkPivotalTitle(activity: Activity, editTextTitle: EditText): Boolean {
        if (editTextTitle.text.toString().isNotEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.pivotal_title_empty)
            )
        }
        return false
    }
}