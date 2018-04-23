package deplink.com.kotlinlearn

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import org.jetbrains.anko.async
import org.jetbrains.anko.find
import org.jetbrains.anko.longToast
import org.jetbrains.anko.uiThread
import java.util.*

class MainActivity : AppCompatActivity() {
    private val items=listOf(
            "Mon 6/23 - Sunny - 31/17",
            "Tue 6/24 - Foggy - 21/8",
            "Wed 6/25 - Coludy - 22/17",
            "Thurs 6/26 - Rainy - 18/11",
            "Fri 6/27 - Foggy - 21/10",
            "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
            "Sun 6/29 - Sunny - 20/7"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val forcastList: RecyclerView=find(R.id.forecast_list)
        forcastList.layoutManager = LinearLayoutManager(this);
        forcastList.adapter =ForecastListAdapter(items)
        async(){
            Request("https://www.baidu.com/").run()
            uiThread { longToast("Request performed") }
        }
        val fl=Forcast(Date(),27.5f,"Shiny day")
        val (data ,tempature,details)=fl
    }
}
