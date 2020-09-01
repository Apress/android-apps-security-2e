package com.redteamlife.aas2.aas2attest

import android.util.Log
import android.widget.Switch
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNet
import org.json.JSONObject


class Attest(activity: MainActivity){

    private val mActivity = activity
    val TAG = "aas2attest"
    val main_url = "https://aas2.redteamlife.com:8443/"

    fun getNonce(){

        val queue = RQueue.getInstance(mActivity.applicationContext).requestQueue
        val url = main_url+"nonce"
        val jsonReq = JsonObjectRequest(Request.Method.GET,url,JSONObject(),Response.Listener {response ->
            requestAttest(response.getString("nonce"))
        },Response.ErrorListener {error ->
            Log.d(TAG,error.toString())
        })
        RQueue.getInstance(mActivity.applicationContext).addToRequestQueue(jsonReq)
    }

    fun requestAttest(nonce:String){
        val API_KEY = "<enter your API key here>"

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mActivity.applicationContext)
            == ConnectionResult.SUCCESS) {
            SafetyNet.getClient(mActivity.applicationContext).attest(nonce.toByteArray(), API_KEY)
                .addOnSuccessListener { resp -> validate(resp.jwsResult) }
                .addOnFailureListener { err -> Log.d(TAG,err.toString()) }
        } else {
            Log.d(TAG,"Install Play Services")
        }
    }


    fun validate(jwsResult: String){
        val queue = RQueue.getInstance(mActivity).requestQueue
        val url = main_url+"validate"
        val jsonObj = JSONObject()
        jsonObj.put("jws_result",jwsResult)
        jsonObj.put("action","change_state")

        val sw = mActivity.findViewById<Switch>(R.id.switch1)
        val jsonReq = JsonObjectRequest(Request.Method.POST,url,jsonObj,Response.Listener {response ->
            sw.isChecked = response.getBoolean("validation")
            sw.isEnabled = true
        },Response.ErrorListener {error ->
            Log.d(TAG,error.toString())
        })
        RQueue.getInstance(mActivity.applicationContext).addToRequestQueue(jsonReq)
    }
}