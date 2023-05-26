package com.finalprogram.yuemiao.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import com.finalprogram.yuemiao.MyApplication
import com.finalprogram.yuemiao.R
import com.finalprogram.yuemiao.network.HttpUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.concurrent.thread


class DownloadService : Service() {

    // 通知管理器
    private lateinit var mManager: NotificationManager

    // 通知构造器
    private lateinit var mBuilder: NotificationCompat.Builder

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化通知管理器
        mManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * 调用startService后触发
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("DownloadService", "onStartCommand executed")
        thread {
            // 处理具体的服务逻辑
            // 创建下载进度条通知
            createNotificationForProgress()
            // 向服务器发起下载请求
            HttpUtil.sendOkHttpRequest("/resource/guide.jpg",
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        // 请求服务器失败，发出下载失败通知
                        createNotificationForHigh("下载失败")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        // 请求成功，将响应的图片写入内存
                        writeToSDCard(response)
                    }
                })
            // 服务完成后自动结束服务
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // 通知的id
    private val progressNotificationId = 1
    private val highNotificationId = 2

    /**
     * 下载完成通知（重要通知，有悬浮弹出提示的通知）
     */
    @SuppressLint("IntentReset")
    private fun createNotificationForHigh(downloadResult: String) {
        // 删除进度条通知
        mManager.cancel(progressNotificationId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "重要通知id",
                "重要通知",
                NotificationManager.IMPORTANCE_HIGH
            )
            mManager.createNotificationChannel(channel)
        }
        mBuilder = NotificationCompat.Builder(MyApplication.context, "重要通知id")
            .setContentTitle(downloadResult)
            .setContentText("接种参考手册$downloadResult")
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.drawable.notice)
            .setAutoCancel(true)
        if (downloadResult == "下载完成") {
            // 打开相册
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val pendingIntent = PendingIntent.getActivity(
                MyApplication.context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            mBuilder.addAction(R.mipmap.ic_launcher, "去看看", pendingIntent)// 通知上的操作
                .setContentIntent(pendingIntent) // 跳转配置
        }
        mManager.notify(highNotificationId, mBuilder.build())
    }

    /**
     * 下载进度条通知
     */
    private fun createNotificationForProgress() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    "进度条id",
                    "进度条通知",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            channel.setShowBadge(true)
            mManager.createNotificationChannel(channel)
        }
        val progressMax = 100
        mBuilder = NotificationCompat.Builder(MyApplication.context, "进度条id")
            .setContentTitle("正在下载接种参考手册")
            .setContentText("开始下载")
            .setSmallIcon(R.drawable.notice)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            // 第3个参数indeterminate，false表示确定的进度，比如100，
            // true表示不确定的进度，会一直显示进度动画，直到更新状态下载完成，或删除通知
            .setProgress(progressMax, progressNotificationId, false)
        mManager.notify(progressNotificationId, mBuilder.build())
    }

    /**
     * 更新下载进度条通知
     *
     * @param progressCurrent 下载进度
     */
    private fun updateNotificationForProgress(progressCurrent: Int) {
        if (::mBuilder.isInitialized) {
            val progressMax = 100
            mBuilder.setContentText("下载中：$progressCurrent%")
                .setProgress(progressMax, progressCurrent, false)
            mManager.notify(progressNotificationId, mBuilder.build())
        }
    }

    /**
     * 将文件写入SD卡来保存
     */
    private fun writeToSDCard(response: Response) {
        //决定存放路径
        // 1.随着app的消失而消失，外部存储  在mnt/sdcard/Android中
        /* val savePath = getExternalFilesDir(null).toString() + File.separator
         val fileName = "wj.jpg"
         val file = File(save_Path,fileName)*/
        // 2.SD卡 不会随着app消失而消失，内部存储
        val savePath =
            Environment.getExternalStorageDirectory().absolutePath + "/Download/"
        val dir = File(savePath) //文件夹
        //文件夹不存在则创建
        if (!dir.exists()) dir.mkdirs()
        val fileName = System.currentTimeMillis().toString()
        val sb = StringBuilder() //连接字符串，形成保存的文件名
        sb.append(fileName).append(".jpg")
        val file = File(dir, sb.toString()) //创建文件
        val fileReader = ByteArray(4096) //读取到磁盘速度
        val fileSize = response.body!!.contentLength() //请求响应的文件资源总大小
        val inputStream: InputStream = response.body?.byteStream()!! //获取资源输入流
        val fileOutputStream = FileOutputStream(file) //文件输出流
        var sum: Long = 0 //当前下载的资源大小
        var read: Int //读取的长度
        while (inputStream.read(fileReader).also { read = it } != -1) {
            fileOutputStream.write(fileReader, 0, read) //写入本地文件
            //获取当前进度并更新下载进度条通知
            sum += read.toLong()
            val progress = (sum * 1.0 / fileSize * 100).toInt()
            updateNotificationForProgress(progress)
        }
        fileOutputStream.flush() //结束后，刷新清空文件流
        createNotificationForHigh("下载完成") //结束后，发送下载成功信息
        // 通知系统媒体库更新
        MediaScannerConnection.scanFile(
            MyApplication.context,
            arrayOf(file.absolutePath),
            null,
            null
        )
        //最后关闭流，防止内存泄露
        inputStream.close()
        fileOutputStream.close()
    }

}