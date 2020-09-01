package com.redteamlife.aas2.aas2obfuscate

import android.util.Log
import java.util.*

class TestModule{
    fun logSomething(){
        Log.d("aas2obfuscate","This is something")
    }

    fun isOddDay()  : Boolean {
        val calendar : Calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_MONTH) % 2 != 0
    }

    fun getNuts(): Array<String> {
        return arrayOf("almonds","peanuts","cashews","hazelnuts")
    }
}