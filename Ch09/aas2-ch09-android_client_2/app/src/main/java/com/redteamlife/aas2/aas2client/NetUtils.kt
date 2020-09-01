package com.redteamlife.aas2.aas2client

import android.net.http.X509TrustManagerExtensions
import android.os.AsyncTask

import android.util.Base64
import android.util.Log


import org.json.JSONObject
import java.io.*
import java.net.URL
import java.security.KeyStore
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*
import javax.security.cert.CertificateException

public class NetworkAsyncTask(activity: MainActivity): AsyncTask<String, Void, String>(){

    private val mActivity = activity

    override fun doInBackground(vararg params: String?): String? {
        val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val ks: KeyStore? = null
        trustManagerFactory.init(ks)
        var x509TrustManager: X509TrustManager? = null
        for(trustManager: TrustManager in trustManagerFactory.trustManagers) run lit@{
            if ( trustManager is  X509TrustManager){
                x509TrustManager = trustManager
                return@lit
            }
        }
        val trustManagerExt = X509TrustManagerExtensions(x509TrustManager)

        var connection: HttpsURLConnection? = null
        return try{
            connection = (URL(params[0])?.openConnection() as? HttpsURLConnection)
            connection?.requestMethod = "POST"
            connection?.doOutput = true
            connection?.doInput = true
            connection?.setRequestProperty("Content-Type","application/json")
            connection?.connect()
            val validPins: Set<String> = Collections.singleton("jM2RG/WsDtG849S7Inoq7tc3O1pyWewWIlH7lFyfrVc=")
            validatePinning(trustManagerExt,connection,validPins)

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

    @Throws(SSLException::class)
    private fun validatePinning(trustManagerExt: X509TrustManagerExtensions, conn: HttpsURLConnection?, validPins: Set<String>)  {
        var certChainMsg: String = ""
        try{
            val md: MessageDigest = MessageDigest.getInstance("SHA-256")
            val trustedChain: List<X509Certificate> = trustedChain(trustManagerExt,conn)
            for( cert: X509Certificate in trustedChain ) run {
                val publicKey: ByteArray = cert.publicKey.encoded;
                md.update(publicKey,0,publicKey.size)

                var pin: String = Base64.encodeToString(md.digest(),Base64.NO_WRAP)
                certChainMsg += "    sha256/" + pin + " : " + cert.subjectDN.toString() + "\n"
                if (validPins.contains(pin)){
                    return;
                }
            }
        } catch(e: NoSuchAlgorithmException){
            throw SSLException(e)
        }
        throw SSLPeerUnverifiedException("Pinning Fail! Chain: \n"+certChainMsg)
    }

    @Throws(SSLException::class)
    private fun trustedChain(trustManagerExt: X509TrustManagerExtensions, conn: HttpsURLConnection?): List<X509Certificate>{
        val serverCerts: Array<Certificate> = conn?.serverCertificates!!
        val untrustedCerts: Array<X509Certificate> = serverCerts.map { it as X509Certificate }.toTypedArray()
        val host: String = conn?.url!!.host
        try{
            return trustManagerExt.checkServerTrusted(untrustedCerts,"RSA", host)
        } catch(e: CertificateException) {
            throw SSLException(e)
        }
    }
}