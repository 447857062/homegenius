package deplink.com.testdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val artist:Artist?=null;
        val name=artist?.name?:"empty"
        val button=findViewById(R.id.button4) as Button
        //lambdas表达式
        button.setOnClickListener{toast("你好kotlin")}
        button3.text="按钮3"
    }
    fun MainActivity.toast(message:CharSequence,duration: Int =Toast.LENGTH_SHORT){
        Toast.makeText(this,message,duration).show()
    }
    fun add(x : Int ,y : Int) : Int = x+y
}
