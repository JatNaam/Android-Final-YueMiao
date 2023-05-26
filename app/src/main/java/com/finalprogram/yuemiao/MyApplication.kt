package com.finalprogram.yuemiao

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.baidu.location.LocationClient
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

/*全局获取Context的方法：
* 还需要在Manifest中进行配置*/
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        // 百度定位SDK隐私政策一定要初始化SDK前设置
        SDKInitializer.setAgreePrivacy(context, true)
        LocationClient.setAgreePrivacy(true)
        // 初始化百度SDK
        SDKInitializer.initialize(context)
        SDKInitializer.setCoordType(CoordType.BD09LL)
    }

    companion object {
        /**
         * 这里获取的context是Application的Context，全局只会存在一份，
         * 应用程序整个生命周期都不会回收，所以不存在内存泄漏的风险，所以使用注释让AS忽略风险
         * */
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        /**
         * Glide请求图片选项配置
         * */
        val requestOptions = RequestOptions.circleCropTransform()
            .diskCacheStrategy(DiskCacheStrategy.NONE) //不做磁盘缓存
            .skipMemoryCache(true) //不做内存缓存
    }
}