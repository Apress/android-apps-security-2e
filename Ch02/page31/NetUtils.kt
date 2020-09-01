package com.redteamlife.aas2.aas2client

import android.os.AsyncTask
import android.util.Log

import org.json.JSONObject
import java.io.*
import java.net.URL
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.*
import java.security.cert.X509Certificate

class NetworkAsyncTask(activity: MainActivity): AsyncTask<String, Void, String>(){

    private val mActivity = activity

    override fun doInBackground(vararg params: String?): String? {
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")

        val caInput: InputStream = BufferedInputStream(mActivity.resources.openRawResource(R.raw.cert))
        val ca: X509Certificate = caInput.use {
            cf.generateCertificate(it) as X509Certificate
        }

        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType).apply {
            load(null, null)
            setCertificateEntry("ca", ca)
        }

        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
            init(keyStore)
        }

        val context: SSLContext = SSLContext.getInstance("TLS").apply {
            init(null, tmf.trustManagers, null)
        }


        var connection: HttpsURLConnection? = null
        return try{
            connection = (URL(params[0])?.openConnection() as? HttpsURLConnection)
            connection?.requestMethod = "POST"
            connection?.doOutput = true
            connection?.doInput = true
            connection?.setRequestProperty("Content-Type","application/json")
            connection?.sslSocketFactory = context.socketFactory
            connection?.connect()

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