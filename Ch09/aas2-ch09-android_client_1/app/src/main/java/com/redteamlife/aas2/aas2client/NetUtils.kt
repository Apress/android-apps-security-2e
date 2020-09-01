package com.redteamlife.aas2.aas2client

import android.os.AsyncTask

import org.json.JSONObject
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection

public class NetworkAsyncTask(activity: MainActivity): AsyncTask<String, Void, String>(){

    private val mActivity = activity

    override fun doInBackground(vararg params: String?): String? {
        var connection: HttpsURLConnection? = null
        return try{
            connection = (URL(params[0])?.openConnection() as? HttpsURLConnection)
            connection?.requestMethod = "POST"
            connection?.doOutput = true
            connection?.doInput = true
            connection?.setRequestProperty("Content-Type","application/json")
            val message : JSONObject = JSONObject()
            message.put("code",params[1])
            val outputWriter = OutputStreamWriter(connection?.outputStream)
            outputWriter.write(message.toString())
            outputWriter.flush()
            if (connection?.responseCode == 200){
                val inputStream = InputStreamReader(connection?.inputStream)
                val body = JSONObject(inputStream.readText())
                return body.getString("message")
            } else{
                return "error"
            }
        } finally {
            connection?.disconnect()
        }
    }

    override fun onPostExecute(result: String){
        mActivity.setText(result)
    }
}