package utils.other

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Logger
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import loggerbird.LoggerBird

/**
 * This class is used for applying Worker
 * @param context is for getting reference from the application context.
 * @param workerParameters is used for getting worker job parameters.
 */
internal class WorkerUtil(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Result.success()
        } catch (error: Throwable) {
            Result.failure()
        }
    }
}


