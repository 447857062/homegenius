package deplink.com.kotlinlearn

import android.util.Log
import java.net.URL

/**
 * Created by ${kelijun} on 2018/4/23.
 */
class Request (val url :String){
    public  fun  run(){
        val forcastjsonStr= URL(url).readText()
        Log.i(javaClass.simpleName,forcastjsonStr)
    }
}