package deneme.example.loggerbird

import android.content.Context
import android.view.View
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.denemekotlinlibrary", appContext.packageName)
    }


    @Test
    fun runSaveASTxt(){
        lateinit var instrumentationContext: Context
        lateinit var instrumentationView:View
        instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext
        val filePath=instrumentationContext.getFilesDir()
//        assertEquals(true,LogDeneme.saveComponentDetails())
    }
}
