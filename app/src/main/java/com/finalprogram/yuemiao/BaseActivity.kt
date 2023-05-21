package com.finalprogram.yuemiao

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.finalprogram.yuemiao.ActivityCollector.addActivity
import com.finalprogram.yuemiao.ActivityCollector.finishAll
import com.finalprogram.yuemiao.ActivityCollector.removeActivity
import com.finalprogram.yuemiao.database.AppDatabase
import com.finalprogram.yuemiao.database.entity.User
import com.finalprogram.yuemiao.ui.login.LoginActivity
import kotlin.concurrent.thread


open class BaseActivity : AppCompatActivity() {
    private var receiver: ForceOfflineReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI和系统状态栏融合
        val decorView: View = window.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        addActivity(this) // 调用ActivityCollector的addActivity方法
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.finalprogram.yuemiao.Force_Offline")
        receiver = ForceOfflineReceiver() // 创建广播接受器对象
        registerReceiver(receiver, intentFilter) // 动态注册广播接收器
    }

    override fun onPause() {
        super.onPause()
        if (receiver != null) {
            unregisterReceiver(receiver) //注销广播接收器
            receiver = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeActivity(this) // 调用ActivityCollector的removeActivity方法
    }

    // 继承广播接受器类
    inner class ForceOfflineReceiver : BroadcastReceiver() {
        /**
         * @param intent 接受广播传输的数据
         */
        override fun onReceive(context: Context, intent: Intent) {
            AlertDialog.Builder(context).apply {
                setTitle("注销")
                setMessage("是否退出当前账号？点击遮罩层取消操作。")
                setCancelable(true)
                setPositiveButton("确认") { _, _ ->
                    finishAll() // 销毁所有活动
                    // 更新当前登录账号的登录状态
                    thread {
                        val userDao = AppDatabase.getDatabase(MyApplication.context).getUserDao()
                        val user = userDao.loadLoggingUser(true) as User
                        user.isLogging = false
                        userDao.updateUser(user)
                    }
                    val intent1 = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent1) // 重新启动LoginActivity
                }
                show()
            }
        }
    }
}