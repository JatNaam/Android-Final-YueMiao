package com.finalprogram.yuemiao.ui.home

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.finalprogram.yuemiao.BaseActivity
import com.finalprogram.yuemiao.MyApplication
import com.finalprogram.yuemiao.databinding.ActivityReferBinding
import com.finalprogram.yuemiao.service.DownloadService
import com.permissionx.guolindev.PermissionX


class ReferActivity : BaseActivity() {


    private lateinit var binding: ActivityReferBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backHome.setOnClickListener { finish() }

        binding.downloadBtn.setOnClickListener {

            // 通过PermissionX插件动态申请写入文件需要的权限
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        Log.d("Location Grant：", grantedList.toString())
                        Toast.makeText(MyApplication.context, "开始下载", Toast.LENGTH_SHORT).show()
                        // 启动下载服务
                        val intent = Intent(this, DownloadService::class.java)
                        startService(intent)

                    } else {
                        Toast.makeText(
                            MyApplication.context,
                            " You denied $deniedList",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}