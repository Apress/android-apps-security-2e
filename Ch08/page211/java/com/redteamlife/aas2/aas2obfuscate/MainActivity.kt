package com.redteamlife.aas2.aas2obfuscate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.util.*
import com.scottyab.rootbeer.RootBeer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val testModule = TestModule()


        if (testModule.isOddDay()) {
            findViewById<TextView>(R.id.oddDay).text = "It's an odd day today."
        }

        val nuts = testModule.getNuts()
        val loop : TextView = findViewById(R.id.loop)
        for (type in nuts){
            loop.append(type+"\n")
        }

        val rootBeer = RootBeer(applicationContext)
        if (rootBeer.isRooted()) {
            Log.d("aas2obfuscate","Device has been rooted!")
        } else {
            Log.d("aas2obfuscate", "No root detected")
        }
    }
}
