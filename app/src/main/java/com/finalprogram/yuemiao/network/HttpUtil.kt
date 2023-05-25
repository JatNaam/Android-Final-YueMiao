package com.finalprogram.yuemiao.network

import okhttp3.OkHttpClient
import okhttp3.Request

object HttpUtil {

    private const val url = "http://139.9.44.141"

    fun sendOkHttpRequest(address: String, callback: okhttp3.Callback) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url + address)
            .build()
        client.newCall(request).enqueue(callback)
    }

}