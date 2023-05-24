package com.finalprogram.yuemiao.ui.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.finalprogram.yuemiao.MainActivity
import com.finalprogram.yuemiao.MyApplication
import com.finalprogram.yuemiao.R
import com.finalprogram.yuemiao.database.entity.User
import com.finalprogram.yuemiao.databinding.FragmentHomeBinding
import com.finalprogram.yuemiao.databinding.HomeBookTopicBinding
import com.finalprogram.yuemiao.databinding.HomeCovTopicBinding
import com.finalprogram.yuemiao.ui.outpatient.MapActivity
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.RectangleIndicator


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var bookTopicBinding: HomeBookTopicBinding

    private lateinit var covTopicBinding: HomeCovTopicBinding

    private var user: User? = null

    private lateinit var mManager: NotificationManager
    private lateinit var mBuilder: NotificationCompat.Builder

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    /**
     * 获取activity的值
     */
    override fun onAttach(context: Context) {
        Log.d("HomeFragment", "onAttach")
        super.onAttach(context)
        (context as MainActivity).getUser().let {
            this.user = it
            Log.d("HomeFragment onAttach", it.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        bookTopicBinding = HomeBookTopicBinding.bind(binding.root)
        covTopicBinding = HomeCovTopicBinding.bind(binding.root)
        Log.d("HomeFragment", "onCreateView")

        // 轮播图
        binding.banner.setAdapter(object : BannerImageAdapter<DataBean>(DataBean.bannerImages) {
            override fun onBindView(
                holder: BannerImageHolder,
                data: DataBean,
                position: Int,
                size: Int
            ) {
                Glide.with(holder.itemView)
                    .load(data.imageRes)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                    .into(holder.imageView)
            }
        })?.addBannerLifecycleObserver(this)?.indicator = RectangleIndicator(context)

        mManager =
            activity?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        bookTopicBinding.kidInoculation.setOnClickListener {
            createNotificationForNormal()
        }

        bookTopicBinding.adultInoculation.setOnClickListener {
            createNotificationForHigh()
        }

        bookTopicBinding.hpvInoculation.setOnClickListener {
            createNotificationForProgress()
        }

        bookTopicBinding.bookRecord.setOnClickListener {
            updateNotificationForProgress(1)
        }

        bookTopicBinding.inoculationRecord.setOnClickListener {
            updateNotificationForProgress(2)
        }

        return binding.root
    }

    /**
     * 普通通知
     */
    private fun createNotificationForNormal() {
        // 适配8.0及以上 创建渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("普通通知id", "普通通知", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "描述"
                    setShowBadge(false) // 是否在桌面显示角标
                }
            mManager.createNotificationChannel(channel)
        }
        // 点击意图 // setDeleteIntent 移除意图
        val intent = Intent(activity, MapActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            MyApplication.context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        // 构建配置
        mBuilder = NotificationCompat.Builder(MyApplication.context, "普通通知id")
            .setContentTitle("儿童接种预约通知") // 标题
            .setContentText("你预约了儿童接种") // 文本
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)) // 大图标
            .setSmallIcon(R.drawable.notice)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 7.0 设置优先级
            .setContentIntent(pendingIntent) // 跳转配置
            .setAutoCancel(true) // 是否自动消失（点击）or mManager.cancel(mNormalNotificationId)、cancelAll、setTimeoutAfter()
        // 发起通知
        mManager.notify(9001, mBuilder.build())
    }

    /**
     * 重要通知
     */
    private fun createNotificationForHigh() {
        val intent = Intent(activity, MapActivity::class.java)
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
            channel.setShowBadge(true)
            mManager.createNotificationChannel(channel)
        }
        mBuilder = NotificationCompat.Builder(MyApplication.context, "重要通知id")
            .setContentTitle("成人接种预约通知")
            .setContentText("你预约了成人接种")
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.drawable.notice)
            .setAutoCancel(true)
            .setNumber(999) // 自定义桌面通知数量
            .addAction(R.mipmap.ic_launcher, "去看看", pendingIntent)// 通知上的操作
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // 通知类别，"勿扰模式"时系统会决定要不要显示你的通知
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE) // 屏幕可见性，锁屏时，显示icon和标题，内容隐藏
        mManager.notify(9002, mBuilder.build())
    }

    /**
     * 进度条通知
     */
    private fun createNotificationForProgress() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("进度条id", "进度条通知", NotificationManager.IMPORTANCE_DEFAULT)
            mManager.createNotificationChannel(channel)
        }
        val progressMax = 100
        val progressCurrent = 30
        mBuilder = NotificationCompat.Builder(MyApplication.context, "进度条id")
            .setContentTitle("HPV预约进度通知")
            .setContentText("预约中：$progressCurrent%")
            .setSmallIcon(R.drawable.notice)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            // 第3个参数indeterminate，false表示确定的进度，比如100，true表示不确定的进度，会一直显示进度动画，直到更新状态下载完成，或删除通知
            .setProgress(progressMax, progressCurrent, false)

        mManager.notify(9003, mBuilder.build())
    }

    /**
     * 更新进度条通知
     * 1.更新进度：修改进度值即可
     * 2.下载完成：总进度与当前进度都设置为0即可，同时更新文案
     *
     * @param op 1为更新进度，2为完成
     */
    private fun updateNotificationForProgress(op: Int) {
        if (::mBuilder.isInitialized) {
            val progressMax = 100
            val progressCurrent = 99
            // 1.更新进度
            if (op == 1)
                mBuilder.setContentText("预约中：$progressCurrent%")
                    .setProgress(progressMax, progressCurrent, false)
            // 2.下载完成
            else if (op == 2)
                mBuilder.setContentText("预约成功！").setProgress(0, 0, false)
            mManager.notify(9003, mBuilder.build())
            Toast.makeText(MyApplication.context, "已更新进度到$progressCurrent%", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(MyApplication.context, "请先发一条进度条通知", Toast.LENGTH_SHORT).show()
        }
    }
}