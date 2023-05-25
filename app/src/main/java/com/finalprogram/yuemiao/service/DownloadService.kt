package com.finalprogram.yuemiao.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
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

    private val mBinder = DownloadBinder()

    class DownloadBinder : Binder()

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    private lateinit var mManager: NotificationManager
    private lateinit var mBuilder: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        mManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("DownloadService", "onStartCommand executed")
        thread {
            // 处理具体的逻辑

            createNotificationForProgress()
            thread {
                HttpUtil.sendOkHttpRequest("/resource/guide.jpg",
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            updateNotificationForProgress(2)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            writeToSDCard(response)
                        }
                    })
            }


            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // 通知的id
    private val progressNotificationId = 1
    private val highNotificationId = 2

    /**
     * 重要通知，有悬浮弹出提示的通知
     */
    private fun createNotificationForHigh(downloadResult: String) {
        // 删除进度条通知
        mManager.cancel(progressNotificationId)
        // 打开文件选择器
        val intent = Intent("android.intent.action.GET_CONTENT")
        // 指定只显示照片
        intent.type = "image/*"
        val pendingIntent = PendingIntent.getActivity(
            MyApplication.context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
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

        if (downloadResult == "下载完成")
            mBuilder.addAction(R.mipmap.ic_launcher, "去看看", pendingIntent)// 通知上的操作
                .setContentIntent(pendingIntent) // 跳转配置
        mManager.notify(highNotificationId, mBuilder.build())
    }

    /**
     * 进度条通知
     */
    private fun createNotificationForProgress() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("进度条id", "进度条通知", NotificationManager.IMPORTANCE_HIGH)
            channel.setShowBadge(true)
            mManager.createNotificationChannel(channel)
        }
        val progressMax = 100
        mBuilder = NotificationCompat.Builder(MyApplication.context, "进度条id")
            .setContentTitle("正在下载接种参考手册")
            .setContentText("开始下载")
            .setSmallIcon(R.drawable.notice)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            // 第3个参数indeterminate，false表示确定的进度，比如100，true表示不确定的进度，会一直显示进度动画，直到更新状态下载完成，或删除通知
            .setProgress(progressMax, progressNotificationId, false)

        mManager.notify(1, mBuilder.build())
    }

    /**
     * 更新进度条通知
     * 1.更新进度：修改进度值即可
     * 2.下载完成：总进度与当前进度都设置为0即可，同时更新文案
     *
     * @param op 1为更新进度，2为下载失败
     */
    private fun updateNotificationForProgress(op: Int, progressCurrent: Int = -1) {
        if (::mBuilder.isInitialized) {
            val progressMax = 100
            // 1.更新进度
            if (op == 1) {
                mBuilder.setContentText("下载中：$progressCurrent%")
                    .setProgress(progressMax, progressCurrent, false)
                mManager.notify(progressNotificationId, mBuilder.build())
            }
            // 2.下载结束
            else if (op == 2) {
                if (progressCurrent == -1) {
                    mBuilder.setContentTitle("下载失败")
                        .setProgress(0, 0, false)
                    mManager.notify(progressNotificationId, mBuilder.build())
                    createNotificationForHigh("下载失败")
                } else createNotificationForHigh("下载完成")
            }

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
        val savePath = Environment.getExternalStorageDirectory().absolutePath + "/Download/"
        //文件夹
        val dir = File(savePath)
        //文件夹不存在则创建
        if (!dir.exists()) {
            dir.mkdirs()
        }
        //连接字符串，形成保存的文件名
        val sb = StringBuilder()
        sb.append(System.currentTimeMillis()).append(".jpg")
        val fileName = sb.toString()
        //创建文件
        val file = File(dir, fileName)
        //输入输出流
        //读取到磁盘速度
        val fileReader = ByteArray(4096)
        //文件资源总大小
        val fileSize = response.body!!.contentLength()
        //当前下载的资源大小
        var sum: Long = 0
        //获取资源
        val inputStream: InputStream = response.body?.byteStream()!!
        //文件输出流
        val fileOutputStream = FileOutputStream(file)
        //读取的长度
        var read: Int
        while (inputStream.read(fileReader).also { read = it } != -1) {
            //写入本地文件
            fileOutputStream.write(fileReader, 0, read)
            //获取当前进度
            sum += read.toLong()
            val progress = (sum * 1.0 / fileSize * 100).toInt()
            // 更新进度消息
            updateNotificationForProgress(1, progress)
        }
        //结束后，刷新清空文件流
        fileOutputStream.flush()
        //结束后，发送下载成功信息
        updateNotificationForProgress(2, 1)
        //最后关闭流，防止内存泄露
        inputStream.close()
        fileOutputStream.close()
    }

}