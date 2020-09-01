package com.redteamlife.aas2.aas2client


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button : Button = findViewById(R.id.button)
        button.setOnClickListener{
            val text : EditText = findViewById(R.id.editText)
            if(!text.text.isEmpty()){
                val networkTask = NetworkAsyncTask(this)
                networkTask.execute("https://aas2.redteamlife.com:8443/secret",text.text.toString())
            }
        }
    }

    fun setText(text: String){
        var loader: TextView = findViewById(R.id.loaderText)
        loader.text = text
    }

}




