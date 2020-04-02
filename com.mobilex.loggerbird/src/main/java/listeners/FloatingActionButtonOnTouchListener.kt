package listeners

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import observers.LogActivityLifeCycleObserver


class FloatingActionButtonOnTouchListener(
    private val floatingActionButtonScreenShot: FloatingActionButton,
    private val floatingActionButtonVideo: FloatingActionButton,
    private val floatingActionButtonAudio: FloatingActionButton
) : View.OnTouchListener {
    private var viewDx: Float = 0F
    private var viewDy: Float = 0F
    private var floatingActionButtonScreenShotDx: Float = 0F
    private var floatingActionButtonScreenShotDy: Float = 0F
    private var floatingActionButtonVideoDx: Float = 0F
    private var floatingActionButtonVideoDy: Float = 0F
    private var floatingActionButtonAudioDx: Float = 0F
    private var floatingActionButtonAudioDy: Float = 0F
    private var lastAction: Int = 0
    private var isOpen = false
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                viewDx = view.x - event.rawX
                viewDy = view.y - event.rawY
                floatingActionButtonScreenShotDx = floatingActionButtonScreenShot.x - event.rawX
                floatingActionButtonScreenShotDy = floatingActionButtonScreenShot.y - event.rawY
                floatingActionButtonVideoDx = floatingActionButtonVideo.x - event.rawX
                floatingActionButtonVideoDy = floatingActionButtonVideo.y - event.rawY
                floatingActionButtonAudioDx = floatingActionButtonAudio.x - event.rawX
                floatingActionButtonAudioDy = floatingActionButtonAudio.y - event.rawY
                lastAction = MotionEvent.ACTION_DOWN
            }
            MotionEvent.ACTION_MOVE -> {
                if ((view.parent as ViewGroup).height > (event.rawY + viewDy + view.height) && (event.rawY + viewDy) > 0) {
                    view.y = event.rawY + viewDy
                }
                if ((view.parent as ViewGroup).width > (event.rawX + viewDx + view.width) && (event.rawX + viewDx) > 0) {
                    view.x = event.rawX + viewDx
                }
                floatingActionButtonScreenShot.y = event.rawY + floatingActionButtonScreenShotDy
                floatingActionButtonVideo.y = event.rawY + floatingActionButtonVideoDy
                floatingActionButtonAudio.y = event.rawY + floatingActionButtonAudioDy
                floatingActionButtonScreenShot.x = event.rawX + floatingActionButtonScreenShotDx
                floatingActionButtonVideo.x = event.rawX + floatingActionButtonVideoDx
                floatingActionButtonAudio.x = event.rawX + floatingActionButtonAudioDx
                lastAction = MotionEvent.ACTION_MOVE

            }
            MotionEvent.ACTION_UP -> {
                if ((view.parent as ViewGroup).width < (event.rawX + viewDx + 3 * (view.width))) {
                    view.x = ((view.parent as ViewGroup).width.toFloat() - view.width)
                    floatingActionButtonScreenShot.x =
                        (view.x - floatingActionButtonScreenShot.width)
                    floatingActionButtonVideo.x = (view.x - floatingActionButtonVideo.width) - 100
                    floatingActionButtonAudio.x = (view.x - floatingActionButtonAudio.width) - 200
                } else if (event.rawX + 3 * (viewDx) < 0) {
                    view.x = 0F
                    floatingActionButtonScreenShot.x =
                        floatingActionButtonScreenShot.width.toFloat()
                    floatingActionButtonVideo.x = floatingActionButtonVideo.width.toFloat() + 100
                    floatingActionButtonAudio.x = floatingActionButtonAudio.width.toFloat() + 200
                }
                if ((view.parent as ViewGroup).height < (event.rawY + viewDy + 3 * (view.height))) {
                    view.y = ((view.parent as ViewGroup).height.toFloat() - view.height)
                    floatingActionButtonScreenShot.y =
                        (view.y - floatingActionButtonScreenShot.height)
                    floatingActionButtonVideo.y = (view.y - floatingActionButtonVideo.height) - 100
                    floatingActionButtonAudio.y = (view.y - floatingActionButtonAudio.height) - 200
                } else if (event.rawY + 3 * (viewDy) < 0) {
                    view.y = 0F
                    floatingActionButtonScreenShot.y =
                        floatingActionButtonScreenShot.height.toFloat()
                    floatingActionButtonVideo.y = floatingActionButtonVideo.height.toFloat() + 100
                    floatingActionButtonAudio.y = floatingActionButtonAudio.height.toFloat() + 200
                }
                LogActivityLifeCycleObserver.floatingActionButtonLastDx = view.x
                LogActivityLifeCycleObserver.floatingActionButtonLastDy = view.y
                LogActivityLifeCycleObserver.floatingActionButtonScreenShotLastDx = floatingActionButtonScreenShot.x
                LogActivityLifeCycleObserver.floatingActionButtonScreenShotLastDy = floatingActionButtonScreenShot.y
                LogActivityLifeCycleObserver.floatingActionButtonVideoLastDx = floatingActionButtonVideo.x
                LogActivityLifeCycleObserver.floatingActionButtonVideoLastDy = floatingActionButtonVideo.y
                LogActivityLifeCycleObserver.floatingActionButtonAudioLastDx = floatingActionButtonAudio.x
                LogActivityLifeCycleObserver.floatingActionButtonAudioLastDy = floatingActionButtonAudio.y
                lastAction = MotionEvent.ACTION_UP
            }
        }


        return false
    }

}