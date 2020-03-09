package deneme.example.filedeneme

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import loggerbird.LoggerBird
import kotlinx.android.synthetic.main.fragment_main4.*

class FragmentMain4 : Fragment() {
    val bundle = Bundle()
    companion object {
        fun newInstance(): FragmentMain4 {
            val fragment = FragmentMain4()
            return fragment
        }
    }
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View =inflater.inflate(R.layout.fragment_main4, container, false)
        LoggerBird.logInit(context!!,fragmentManager = fragmentManager)

        bundle.putString("id", "1")
        bundle.putString("item", "berk")
        bundle.putString("type", "component")
        LoggerBird.takeFragmentManagerDetails(fragmentManager = fragmentManager)
        LoggerBird.saveFragmentManagerDetails()

//        LogDeneme.saveAnalyticsDetails(bundle=bundle)
//        LogDeneme.logAttach()
//        LogDeneme.logFragmentAttach()

        return view
    }


    override fun onStart() {
        super.onStart()
        //            fragmentManager?.beginTransaction()
//                ?.replace(R.id.main_activity_2, FragmentMain3.newInstance(), "FragmentMain3")
//                ?.commit ()
        for(i in fragmentManager!!.fragments){
            Log.d("fragment_names",i.tag)
        }
//        LogDeneme.saveFragmentManagerDetails(fragmentManager = fragmentManager)


        for(i in fragmentManager!!.fragments){
            Log.d("new_fragment_list",i.tag)
        }
        button_dummy_3.setOnClickListener(){
            LoggerBird.saveLifeCycleDetails()
//            LogDeneme.saveLifeCycleDetails()
//            for(i in fragmentManager!!.fragments){
//                fragmentManager!!.beginTransaction().remove(i).commitNow()
//                LogDeneme.saveFragmentManagerDetails(fragmentManager = fragmentManager)
//                break
//            }

//            fragmentManager?.popBackStack(fragmentManager!!.fragments.get(0).tag,0)



        }
    }
}



