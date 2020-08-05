package loggerbird.observers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.mobilex.loggerbird.R
import loggerbird.listeners.layouts.LayoutOnTouchListener

internal class LogComponentObserver {
    private lateinit var viewLoggerBirdCoordinator: View
    private val arrayListComponentViews:ArrayList<View> = ArrayList()
    private lateinit var layoutOnTouchActivityListener: LayoutOnTouchListener
    private lateinit var layoutOnTouchFragmentListener: LayoutOnTouchListener
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeLoggerBirdCoordinatorLayout(activity: Activity? = null , fragment:Fragment? = null){
//        removeLoggerBirdCoordinatorLayout(activity = activity,fragment = fragment)
        if(activity != null){
            val layoutInflater: LayoutInflater = (activity.applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            viewLoggerBirdCoordinator = layoutInflater.inflate(R.layout.loggerbird_coordinator,activity.window.decorView.findViewById(android.R.id.content),true)
            val frameLayout = viewLoggerBirdCoordinator.findViewById<FrameLayout>(R.id.logger_bird_coordinator)
            layoutOnTouchActivityListener = LayoutOnTouchListener(activity = activity)
            frameLayout.setOnTouchListener(layoutOnTouchActivityListener)
            gatherComponentViews(activity = activity)
        }else if(fragment!= null){
            val layoutInflater: LayoutInflater = (fragment.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            viewLoggerBirdCoordinator = layoutInflater.inflate(R.layout.loggerbird_coordinator,(fragment.view as ViewGroup),true)
            val frameLayout = viewLoggerBirdCoordinator.findViewById<FrameLayout>(R.id.logger_bird_coordinator)
            layoutOnTouchFragmentListener = LayoutOnTouchListener(fragment = fragment)
            frameLayout.setOnTouchListener(layoutOnTouchFragmentListener)
            gatherComponentViews(fragment = fragment)
        }
    }

    internal fun removeLoggerBirdCoordinatorLayout(activity: Activity? = null , fragment: Fragment? = null){
        if(this::viewLoggerBirdCoordinator.isInitialized){
            if(activity != null){
                activity.windowManager.removeViewImmediate(viewLoggerBirdCoordinator)
            }else if (fragment != null){
                (fragment.view as ViewGroup).removeView(viewLoggerBirdCoordinator)
            }
        }
    }

    private fun gatherComponentViews(activity: Activity? = null, fragment: Fragment? = null){
        arrayListComponentViews.clear()
        if(activity!= null){
            (activity.window.decorView as ViewGroup).getAllViews().forEach {
                arrayListComponentViews.add(it)
            }
            LogActivityLifeCycleObserver.hashMapActivityComponents[activity] = arrayListComponentViews
        }else if(fragment != null){
            (fragment.view as ViewGroup).getAllViews().forEach {
                arrayListComponentViews.add(it)
            }
            LogFragmentLifeCycleObserver.hashMapFragmentComponents[fragment] = arrayListComponentViews
        }
    }
    private fun View.getAllViews(): List<View> {
        if (this !is ViewGroup || childCount == 0) return listOf(this)
        return children
            .toList()
            .flatMap { it.getAllViews() }
            .plus(this as View)
    }
}