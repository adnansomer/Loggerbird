package loggerbird.utils.api.bitbucket

import loggerbird.adapter.recyclerView.api.bitbucket.RecyclerViewBitbucketAttachmentAdapter
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.JsonObject
import com.mobilex.loggerbird.R
import loggerbird.constants.Constants
import loggerbird.exception.LoggerBirdException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import loggerbird.LoggerBird
import loggerbird.models.AccountIdService
import okhttp3.*
import loggerbird.services.LoggerBirdService
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import loggerbird.utils.other.DefaultToast
import loggerbird.utils.other.InternetConnectionUtil

/** Loggerbird Bitbucket api configuration class **/
internal class BitbucketApi {
    //Global variables.
    private lateinit var activity: Activity
    private lateinit var context: Context
    private var filePathMedia: File? = null
    private val coroutineCallOkHttpBitbucket = CoroutineScope(Dispatchers.IO)
    private val coroutineCallBitbucket = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private var queueCounter = 0
    private lateinit var timerTaskQueue: TimerTask
    private val arrayListProjectNames: ArrayList<String> = ArrayList()
    private val arrayListProjectId: ArrayList<String> = ArrayList()
    private val arrayListAssigneeNames: ArrayList<String> = ArrayList()
    private val arrayListAssigneeAccountId: ArrayList<String> = ArrayList()
    private val arrayListPriorityNames: ArrayList<String> = ArrayList()
    private val arrayListKindNames: ArrayList<String> = ArrayList()
    private val arrayListKindId: ArrayList<String> = ArrayList()
    private var projectPosition = 0
    private var kindPosition = 0
    private var assigneePosition = 0
    private var project: String? = null
    private var projectId: String? = null
    private var assignee: String? = null
    private var priority: String? = null
    private var kind: String? = null
    private var description: String? = null
    private var title: String? = null
    private val defaultToast = DefaultToast()
    /**
     * This method is used for calling an bitbucket action with network connection check.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param task is for getting reference of which asana action will be executed.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws LoggerBirdException if network connection error occurs.
     * @throws exception if error occurs.
     * @see bitbucketExceptionHandler method.
     */
    internal fun callBitbucket(
        activity: Activity,
        context: Context,
        task: String,
        filePathMedia: File? = null
    ) {
        this.activity = activity
        this.context = context
        this.filePathMedia = filePathMedia
        coroutineCallOkHttpBitbucket.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    checkQueueTime(activity = activity)
                    okHttpBitbucketAuthentication(
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
                bitbucketExceptionHandler(e = e)
            }
        }
    }


    /**
     * This method is used for calling an bitbucket action with internet connection check.
     * @param context is for getting reference from the application context.
     * @param activity is used for getting reference of current activity.
     * @param task is for getting reference of which bitbucket action will be executed.
     * @param filePathMediaName is used for getting the reference of current media file.
     * @throws LoggerBirdException if internet connection error occurs.
     * @throws exception if error occurs.
     * @see bitbucketExceptionHandler method.
     */
    private fun okHttpBitbucketAuthentication(
        context: Context,
        activity: Activity,
        task: String,
        filePathMediaName: File?
    ) {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://bitbucket.com")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                bitbucketExceptionHandler(e = e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("response_message", response.message)
                Log.d("response_code", response.code.toString())
                try {
                    if (response.code in 200..299) {
                        coroutineCallBitbucket.async {
                            try {
                                when (task) {
                                    "create" -> bitbucketCreateIssue(activity = activity)
                                    "get" -> gatherBitbucketDetails()
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                LoggerBird.callEnqueue()
                                LoggerBird.callExceptionDetails(
                                    exception = e,
                                    tag = Constants.bitbucketTag
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
                    bitbucketExceptionHandler(e = e)
                }
            }
        })
    }

    /**
     * This method is used for creating bitbucket issue.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs.
     * @see bitbucketExceptionHandler method.
     */
    private fun bitbucketCreateIssue(activity: Activity) {
        try {
            queueCounter = 0
            queueCounter++
            this.activity = activity
            val jsonObject = JsonObject()
            val jsonObjectDescription = JsonObject()
            val stringBuilder = StringBuilder()
            if (!description.isNullOrEmpty()) {
                stringBuilder.append(description + "\n")
            }
            stringBuilder.append("Life Cycle Details:" + "\n")
            var classCounter = 0
            LoggerBird.classPathList.forEach {
                stringBuilder.append("$it (${LoggerBird.classPathListCounter[classCounter]})\n")
                classCounter++
            }
            if (!assignee.isNullOrEmpty()) {
                val jsonObjectAssignee = JsonObject()
                jsonObjectAssignee.addProperty("account_id",arrayListAssigneeAccountId[assigneePosition])
                jsonObject.add("assignee",jsonObjectAssignee)
            }
            jsonObjectDescription.addProperty("markup","plaintext")
            jsonObjectDescription.addProperty("raw",stringBuilder.toString())
            jsonObject.add("content",jsonObjectDescription)
            jsonObject.addProperty("priority", priority)
            jsonObject.addProperty("kind", kind)
            jsonObject.addProperty("title", title)
            RetrofitBitbucketClient.getBitbucketUserClient(url = "https://api.bitbucket.org/2.0/repositories/${LoggerBird.bitbucketUserName}/${arrayListProjectNames[projectPosition]}/")
                .create(AccountIdService::class.java)
                .createBitbucketIssues(jsonObject = jsonObject)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        bitbucketExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        val coroutineCallBitbucketAttachments = CoroutineScope(Dispatchers.IO)
                        Log.d("bitbucket_details", response.code().toString())
                        val bitbucketList = response.body()
                        if (bitbucketList != null) {
                            RecyclerViewBitbucketAttachmentAdapter.ViewHolder.arrayListFilePaths.forEach {
                                queueCounter++
                                coroutineCallBitbucketAttachments.async {
                                    createAttachments(
                                        activity = activity,
                                        file = it.file,
                                        issueKey = bitbucketList["id"].asString
                                    )
                                }
                            }
                        }
                        resetBitbucketValues(shareLayoutMessage = "bitbucket")
                    }
                })

        } catch (e: Exception) {
            bitbucketExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for adding creating attachment when bitbucket issue created.
     * @param issueKey is used for getting reference of current created issue id.
     * @param file is used for getting reference of the current file.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs.
     * @see bitbucketExceptionHandler method.
     */
    private fun createAttachments(
        issueKey: String,
        file: File,
        activity: Activity
    ) {
        try {
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            RetrofitBitbucketClient.getBitbucketUserClient(url = "https://api.bitbucket.org/2.0/repositories/${LoggerBird.bitbucketUserName}/${arrayListProjectNames[projectPosition]}/issues/$issueKey/")
                .create(AccountIdService::class.java)
                .setBitbucketAttachments(
                    file = body
                )
                .enqueue(object : retrofit2.Callback<Void> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<Void>,
                        t: Throwable
                    ) {
                        bitbucketExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<Void>,
                        response: retrofit2.Response<Void>
                    ) {
                        val coroutineCallBitbucketAttachments = CoroutineScope(Dispatchers.IO)
                        coroutineCallBitbucketAttachments.async {
                            val bitbucketResponse = response.body()
                            Log.d("attachment_put_success", response.code().toString())
                            Log.d("attachment_put_success", response.message())
                            resetBitbucketValues(shareLayoutMessage = "bitbucket")
                        }
                    }
                })
        } catch (e: Exception) {
            bitbucketExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for initializing the gathering action of bitbucket.
     * @throws exception if error occurs.
     * @see bitbucketExceptionHandler method.
     */
    private fun gatherBitbucketDetails() {
        try {
            queueCounter = 0
            arrayListKindNames.clear()
            arrayListKindId.clear()
            arrayListPriorityNames.clear()
            arrayListAssigneeNames.clear()
            arrayListAssigneeAccountId.clear()
            arrayListProjectNames.clear()
            arrayListProjectId.clear()
            gatherTaskPriority()
            gatherTaskKind()
            gatherTaskProject()
            gatherTaskAssignee()
        } catch (e: Exception) {
            bitbucketExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for getting project details for bitbucket.
     * @throws exception if error occurs.
     * @see bitbucketExceptionHandler method.
     */
    private fun gatherTaskProject() {
        queueCounter++
        RetrofitBitbucketClient.getBitbucketUserClient(url = "https://api.bitbucket.org/2.0/repositories/")
            .create(AccountIdService::class.java)
            .getBitbucketProjects(userName = LoggerBird.bitbucketUserName)
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    bitbucketExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    Log.d("bitbucket_project_succ", response.code().toString())
                    val bitbucketList = response.body()
                    if (response.code() !in 200..299) {
                        resetBitbucketValues(shareLayoutMessage = "bitbucket_error")
                    } else {
                        val coroutineCallBitbucketProject = CoroutineScope(Dispatchers.IO)
                        coroutineCallBitbucketProject.async {
                            bitbucketList?.getAsJsonArray("values")?.forEach {
                                arrayListProjectNames.add(it.asJsonObject["name"].asString)
                            }
                            updateFields()
                        }
                    }
                }
            })
    }

    /**
     * This method is used for getting assignee details for bitbucket.
     * @throws exception if error occurs.
     * @see bitbucketExceptionHandler method.
     */
    private fun gatherTaskAssignee() {
        queueCounter++
        RetrofitBitbucketClient.getBitbucketUserClient(url = "https://api.bitbucket.org/2.0/workspaces/${LoggerBird.bitbucketUserName}/")
            .create(AccountIdService::class.java)
            .getBitbucketAssignees()
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    bitbucketExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    if (response.code() !in 200..299) {
                        resetBitbucketValues(shareLayoutMessage = "bitbucket_error")
                    } else {
                        val coroutineCallBitbucketAssignee = CoroutineScope(Dispatchers.IO)
                        coroutineCallBitbucketAssignee.async {
                            Log.d("bitbucket_assignee_succ", response.code().toString())
                            val bitbucketList = response.body()
                            bitbucketList?.getAsJsonArray("values")?.forEach {
                                arrayListAssigneeNames.add(it.asJsonObject["user"].asJsonObject["display_name"].asString)
                                arrayListAssigneeAccountId.add(it.asJsonObject["user"].asJsonObject["account_id"].asString)
                            }
                            updateFields()

                        }
                    }
                }
            })
    }

    /**
     * This method is used for getting priority details for bitbucket.
     */
    private fun gatherTaskPriority() {
        arrayListPriorityNames.add("trivial")
        arrayListPriorityNames.add("minor")
        arrayListPriorityNames.add("major")
        arrayListPriorityNames.add("critical")
        arrayListPriorityNames.add("blocker")
    }

    /**
     * This method is used for getting kind details for bitbucket.
     */
    private fun gatherTaskKind() {
        arrayListKindNames.add("bug")
        arrayListKindNames.add("enhancement")
        arrayListKindNames.add("proposal")
        arrayListKindNames.add("task")
    }

    /**
     * This method is used for getting details of autoCompleteTextViews in the bitbucket layout.
     * @param autoTextViewProject is used for getting project details from project autoCompleteTextView in the bitbucket layout.
     * @param autoTextViewAssignee is used for getting assignee details from assignee autoCompleteTextView in the bitbucket layout.
     * @param autoTextViewPriority is used for getting priority details from priority autoCompleteTextView in the bitbucket layout.
     * @param autoTextViewKind is used for getting kind details from kind autoCompleteTextView in the bitbucket layout.
     */
    internal fun gatherAutoTextDetails(
        autoTextViewProject: AutoCompleteTextView,
        autoTextViewAssignee: AutoCompleteTextView,
        autoTextViewPriority: AutoCompleteTextView,
        autoTextViewKind: AutoCompleteTextView
    ) {
        project = autoTextViewProject.editableText.toString()
        assignee = autoTextViewAssignee.editableText.toString()
        priority = autoTextViewPriority.editableText.toString()
        kind = autoTextViewKind.editableText.toString()
    }

    /**
     * This method is used for getting details of editTexts in the bitbucket layout.
     * @param editTextTitle is used for getting title details from title editText in the bitbucket layout.
     * @param editTextDescription is used for getting description details from description editText in the bitbucket layout.
     */
    internal fun gatherEditTextDetails(
        editTextTitle: EditText,
        editTextDescription: EditText
    ) {
        title = editTextTitle.text.toString()
        description = editTextDescription.text.toString()
    }

    /**
     * This method is used for updating and controlling the queue of background tasks in the bitbucket actions.
     */
    private fun updateFields() {
        queueCounter--
        Log.d("que_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeBitbucketAutoTextViews(
                    arrayListBitbucketProject = arrayListProjectNames,
                    arrayListBitbucketAssignee = arrayListAssigneeNames,
                    arrayListBitbucketKind = arrayListKindNames,
                    arrayListBitbucketPriority = arrayListPriorityNames
                )
            }
        }

    }


    /**
     * This method is used for controlling the time of background tasks in the bitbucket actions.
    If tasks will last longer than three minutes then bitbucket layout will be removed.
     */
    private fun checkQueueTime(activity: Activity) {
        val timerQueue = Timer()
        timerTaskQueue = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread {
                    LoggerBirdService.loggerBirdService.finishShareLayout("bitbucket_error_time_out")
                }
            }
        }
        timerQueue.schedule(timerTaskQueue, 180000)
    }

    /**
     * This method is used for default loggerbird.exception handling of bitbucket class.
     * @param e is used for getting reference of loggerbird.exception.
     * @param throwable is used for getting reference of throwable.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun bitbucketExceptionHandler(
        e: Exception? = null,
        throwable: Throwable? = null
    ) {
        resetBitbucketValues(shareLayoutMessage = "bitbucket_error")
        if (this::timerTaskQueue.isInitialized) {
            timerTaskQueue.cancel()
        }
        LoggerBirdService.loggerBirdService.finishShareLayout("bitbucket_error")
        throwable?.printStackTrace()
        e?.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.bitbucketTag,
            throwable = throwable
        )
    }

    /**
     * This method is used for getting reference of current project position in the project autoCompleteTextView in the bitbucket layout.
     * @param projectPosition is used for getting reference of project position.
     */
    internal fun setProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }

    /**
     * This method is used for getting reference of current assignee position in the assignee autoCompleteTextView in the asana layout.
     * @param assigneePosition is used for getting reference of assignee position.
     */
    internal fun setAssignee(assigneePosition: Int) {
        this.assigneePosition = assigneePosition
    }

    /**
     * This method is used for resetting the values in asana action.
     * @param shareLayoutMessage is used for getting reference of the asana action message.
     */
    private fun resetBitbucketValues(shareLayoutMessage: String) {
        queueCounter--
        Log.d("queue_counter", queueCounter.toString())
        if (queueCounter == 0) {
            RecyclerViewBitbucketAttachmentAdapter.ViewHolder.arrayListFilePaths.forEach {
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
            arrayListAssigneeAccountId.clear()
            arrayListPriorityNames.clear()
            arrayListKindNames.clear()
            arrayListKindId.clear()
            projectPosition = 0
            kindPosition = 0
            project = null
            projectId = null
            assignee = null
            priority = null
            kind = null
            description = null
            title = null
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.finishShareLayout(shareLayoutMessage)
            }
        }
    }

    /**
     * This method is used for checking project reference exist in the project list or not empty in the project AutoCompleteTextView field in the bitbucket layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewProject is used for getting reference of project autoCompleteTextView in the bitbucket layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkBitbucketProject(
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
                toastMessage = activity.resources.getString(R.string.bitbucket_project_empty)
            )
        } else if (!arrayListProjectNames.contains(autoTextViewProject.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.bitbucket_project_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking title reference is not empty in the title editText field in the title layout.
     * @param activity is used for getting reference of current activity.
     * @param editTextTitle is used for getting reference of title editText in the bitbucket layout.
     * @return Boolean value.
     */
    internal fun checkBitbucketTitle(
        activity: Activity,
        editTextTitle: EditText
    ): Boolean {
        if (editTextTitle.text.toString().isNotEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.bitbucket_title_empty)
            )
        }
        return false
    }

    /**
     * This method is used for checking priority reference exist in the priority list or not empty in the priority AutoCompleteTextView field in the bitbucket layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewPriority is used for getting reference of priority autoCompleteTextView in the bitbucket layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkBitbucketPriority(
        activity: Activity,
        autoTextViewPriority: AutoCompleteTextView
    ): Boolean {
        if (autoTextViewPriority.editableText.toString().isNotEmpty() && arrayListPriorityNames.contains(
                autoTextViewPriority.editableText.toString()
            )
        ) {
            return true
        } else if (autoTextViewPriority.editableText.toString().isEmpty()) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.bitbucket_priority_empty)
            )
        } else if (!arrayListPriorityNames.contains(autoTextViewPriority.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.bitbucket_priority_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking kind reference exist in the kind list or not empty in the kind AutoCompleteTextView field in the bitbucket layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewKind is used for getting reference of kind autoCompleteTextView in the bitbucket layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkBitbucketKind(
        activity: Activity,
        autoTextViewKind: AutoCompleteTextView
    ): Boolean {
        if (autoTextViewKind.editableText.toString().isNotEmpty() && arrayListKindNames.contains(
                autoTextViewKind.editableText.toString()
            )
        ) {
            return true
        } else if (autoTextViewKind.editableText.toString().isEmpty()) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.bitbucket_kind_empty)
            )
        } else if (!arrayListKindNames.contains(autoTextViewKind.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.bitbucket_kind_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking assignee reference exist in the assignee list in the assignee AutoCompleteTextView field in the bitbucket layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewAssignee is used for getting reference of assignee autoCompleteTextView in the bitbucket layout.
     * @return Boolean value.
     */
    internal fun checkBitbucketAssignee(
        activity: Activity,
        autoTextViewAssignee: AutoCompleteTextView
    ): Boolean {
        if (arrayListAssigneeNames.contains(autoTextViewAssignee.editableText.toString()) || autoTextViewAssignee.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.bitbucket_assignee_doesnt_exist)
            )
        }
        return false
    }

}