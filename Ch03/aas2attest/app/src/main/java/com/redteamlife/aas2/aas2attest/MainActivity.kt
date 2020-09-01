package com.redteamlife.aas2.aas2attest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Switch
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val TAG = "aas2attest"

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val attest = Attest(this)

        val switch = findViewById<Switch>(R.id.switch1)
        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                switch.isEnabled = false
                attest.getNonce()
            }
        }
    }
}
