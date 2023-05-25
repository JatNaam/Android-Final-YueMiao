package com.finalprogram.yuemiao

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.viewpager2.widget.ViewPager2
import com.finalprogram.yuemiao.database.AppDatabase
import com.finalprogram.yuemiao.database.entity.User
import com.finalprogram.yuemiao.databinding.ActivityMainBinding
import com.finalprogram.yuemiao.network.NetWorkStatusChangerReceiver
import com.finalprogram.yuemiao.ui.home.HomeFragment
import com.finalprogram.yuemiao.ui.my.MyFragment
import com.finalprogram.yuemiao.ui.outpatient.OutpatientFragment
import com.finalprogram.yuemiao.ui.tab.Tab
import com.finalprogram.yuemiao.ui.tab.TabStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.concurrent.thread

class MainActivity : BaseActivity() {

    // 不可变列表List
    private val tabList: List<Tab> = listOf(
        Tab(R.drawable.home, "首页"),
        Tab(R.drawable.outpatient, "接种门诊"),
        Tab(R.drawable.my, "我的")
    )

    private val fragmentList: List<Fragment> = listOf(
        HomeFragment.newInstance(),
        OutpatientFragment.newInstance(),
        MyFragment.newInstance()
    )

    private var user: User? = null
    private var comeFromLogin = false

    private var receiver: NetWorkStatusChangerReceiver? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 登录活动跳转传递的账号信息
        user = intent.getSerializableExtra("userData") as User?
        if (user != null) {
            comeFromLogin = true
            Log.d("mainActivity-from-Login", user.toString())
        } else {
            // 重新打开程序时，获取之前正在登录的账号，保持登录
            thread {
                val userDao = AppDatabase.getDatabase(MyApplication.context).getUserDao()
                user = userDao.loadLoggingUser(true)
                if (user == null)
                    runOnUiThread {
                        Toast.makeText(
                            MyApplication.context, "请先登录",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
        initViewPager()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("mainActivity-", "onDestroy")
    }

    /**
     * 初始化viewpager2和tabLayout
     */
    private fun initViewPager() {
        //viewPager2的适配器
        binding.viewPager.adapter = TabStateAdapter(
            this.supportFragmentManager,
            fragmentList,
            // 给lifecycle传生命周期参数，控制fragment预加载的最大生命周期到RESUMED
            LifecycleRegistry(this).apply {
                currentState = Lifecycle.State.RESUMED
            })
        // viewPager2的预加载机制，这里设置为1表示只预加载当前item左右两边
        binding.viewPager.offscreenPageLimit = 1
        //绑定tabLayout和viewPager
        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab, position ->
            tab.text = tabList[position].text
            tab.setIcon(tabList[position].icon)
        }.attach()

        // 从登录活动跳转来
        if (comeFromLogin) {
            binding.viewPager.setCurrentItem(2, false)
        }

        // 监听viewpager2的切换事件
        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val fragment = supportFragmentManager.findFragmentByTag("f$position")
                Log.d("mainActivity-viewpager2:", fragment.toString())
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        receiver = NetWorkStatusChangerReceiver() //创建广播接受器对象
        registerReceiver(receiver, intentFilter) //动态注册广播接收器
    }

    override fun onPause() {
        super.onPause()
        if (receiver != null) {
            // 注销广播接收器
            unregisterReceiver(receiver)
            receiver = null
        }
    }


    /**
     * 给子fragment传值，和fragment的onAttach方法搭配使用是最佳的方法，bundle传值无法即时获得
     */
    fun getUser(): User? {
        return user
    }

}