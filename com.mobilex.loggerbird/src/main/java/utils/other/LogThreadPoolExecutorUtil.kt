import java.util.concurrent.BlockingQueue
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

//thread class might be useful for future.
internal class LogThreadPoolExecutorUtil(
    corePoolSize: Int,
    maximumPoolSize: Int,
    keepAliveTime: Long,
    unit: TimeUnit?,
    workQueue: BlockingQueue<Runnable>?
) : ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue) {
    val workQueueUtil: BlockingQueue<Runnable>? = workQueue
    override fun getQueue(): BlockingQueue<Runnable> {
        return super.getQueue()
    }

    override fun execute(command: Runnable) {
        super.execute(command)
    }

    override fun beforeExecute(t: Thread?, r: Runnable?) {
        super.beforeExecute(t, r)
    }

    override fun afterExecute(r: Runnable?, t: Throwable?) {
        super.afterExecute(r, t)
    }

    override fun submit(task: Runnable): Future<*> {
        return super.submit(task)
    }
}