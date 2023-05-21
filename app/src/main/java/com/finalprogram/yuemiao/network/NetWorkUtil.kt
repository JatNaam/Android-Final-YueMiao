package com.finalprogram.yuemiao.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetWorkUtil {
    companion object {
        // 判断网络是否连接
        fun isNetworkConnected(context: Context?): Boolean {
            if (context != null) {
                // 获取系统网络的Manager对象
                val mConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                // SDK版本判断，以API23为界限
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    val mNetworkInfo = mConnectivityManager.activeNetworkInfo
                    if (mNetworkInfo != null)
                        return mNetworkInfo.isAvailable
                } else {
                    val network = mConnectivityManager.activeNetwork ?: return false
                    val status = mConnectivityManager.getNetworkCapabilities(network)
                        ?: return false
                    if (status.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
                        return true
                }
            }
            return false
        }
    }
}