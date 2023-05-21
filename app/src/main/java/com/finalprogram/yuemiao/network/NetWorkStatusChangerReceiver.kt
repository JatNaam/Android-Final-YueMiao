package com.finalprogram.yuemiao.network

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.finalprogram.yuemiao.ActivityCollector
import com.finalprogram.yuemiao.MyApplication
import com.finalprogram.yuemiao.ui.login.LoginActivity

//继承广播接受器类
class NetWorkStatusChangerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!NetWorkUtil.isNetworkConnected(MyApplication.context)) {
            AlertDialog.Builder(context).apply {
                setTitle("通知")
                setMessage("请检查你的网络是否正常")
                setCancelable(false)
                setPositiveButton("重新登录") { _, _ ->
                    ActivityCollector.finishAll() // 销毁所有活动
                    val intent1 = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent1) // 重新启动LoginActivity
                }
                show()
            }
        }
    }
}