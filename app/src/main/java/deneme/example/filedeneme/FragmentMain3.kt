package deneme.example.filedeneme

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import loggerbird.constants.Constants.Companion.fragmentTag
import loggerbird.LoggerBird
import kotlinx.android.synthetic.main.fragment_main3.*

class FragmentMain3 : Fragment() {
    companion object {

        val fragmentTAG = FragmentMain3::class.java.name

        fun newInstance(): FragmentMain3 {
            val fragment = FragmentMain3()
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_main3, container, false)
        Log.d("fragment_name",fragmentTag)
        Log.d("super_class", FragmentMain3::class.java.superclass!!.simpleName)
        LoggerBird.logInit(context!!,fragmentManager = fragmentManager)
//        LogDeneme.logAttach()
//        LogDeneme.logFragmentAttach()
        return view
    }


    override fun onStart() {
        super.onStart()
        button_dummy_2.setOnClickListener() {
            LoggerBird.saveLifeCycleDetails()
            fragmentManager?.beginTransaction()
                ?.add(
                    R.id.main_activity_2,
                    FragmentMain4.newInstance(), "FragmentMain4")
                ?.commit ()
//            Log.d("fragment",LogDeneme.logFragmentDetails(context!!,FragmentMain3.javaClass))
        }
    }

}
