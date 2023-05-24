package com.finalprogram.yuemiao.ui.register

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.finalprogram.yuemiao.MyApplication
import com.finalprogram.yuemiao.R
import com.finalprogram.yuemiao.database.AppDatabase
import com.finalprogram.yuemiao.database.entity.User
import com.finalprogram.yuemiao.databinding.FragmentRegisterBinding
import com.finalprogram.yuemiao.network.NetWorkUtil
import com.finalprogram.yuemiao.ui.login.LoginActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null

    private val binding get() = _binding!!

    private val takePhoto = 1
    private val fromAlbum = 2
    private lateinit var avatarUri: Uri
    private lateinit var avatarFile: File
    private lateinit var avatar: String
    private var isSetAvatar: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val activity = activity
        // 返回键
        binding.backLogin.setOnClickListener { activity?.finish() }
        // 获取数据库的Dao对象
        val userDao = AppDatabase.getDatabase(MyApplication.context).getUserDao()
        // 表单验证
        binding.registerAccountEdit.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.length !in 6..10) {
                binding.registerAccountEdit.error = "输入不可小于6位且不可超出10位"
            }
        }
        binding.registerPasswordEdit.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.length !in 8..15) {
                binding.registerPasswordEdit.error = "输入不可小于8位且不可超出15位"
            }
        }
        binding.registerNameEdit.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isEmpty()) {
                binding.registerNameEdit.error = "用户名不可为空"
            }
        }
        // 头像选择
        binding.avatar.setOnClickListener {
            setAvatar()
        }
        // 确认注册
        binding.confirmBtn.setOnClickListener {
            val account: String = binding.registerAccountEdit.text.toString()
            val password: String = binding.registerPasswordEdit.text.toString()
            val userName: String = binding.registerNameEdit.text.toString()
            if (account.length in 6..10 && password.length in 8..15 && userName.isNotEmpty() && isSetAvatar) {
                // 网络验证
                if (!NetWorkUtil.isNetworkConnected(MyApplication.context))
                    Toast.makeText(
                        MyApplication.context, "请检查你的网络状态!",
                        Toast.LENGTH_SHORT
                    ).show()
                else thread {
                    /**
                     * 数据库操作必须要在子线程进行
                     */
                    // 查询该账号是否已被占用
                    val user: User? = userDao.loadUser(account)
                    if (user == null) {
                        val newUser = User(
                            account, password, userName,
                            isChecked = true,
                            isLogged = false,
                            avatar,
                            isLogging = false
                        )
                        // 插入成功会返回id
                        newUser.id = userDao.insertUser(newUser)
                        // 注册成功，跳转回登录界面
                        val intent = Intent(context, LoginActivity::class.java).apply {
                            putExtra("registerAccount", account)
                            putExtra("STATUS", "REGISTER")
                        }
                        // 这里的上层活动的UI和数据没有什么需要保留的，直接以SingleTask的模式跳转活动即可
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        activity?.finish() //跳转后，销毁返回键的日志
                    } else
                        activity?.runOnUiThread {
                            Toast.makeText(
                                MyApplication.context, "账号已存在!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            } else
                Toast.makeText(
                    MyApplication.context, "检查你的注册信息是否正确!",
                    Toast.LENGTH_SHORT
                ).show()
        }

        return binding.root
    }

    /**
     * 更换头像
     * */
    @SuppressLint("InflateParams")
    private fun setAvatar() {
        // 弹窗
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_bottom_register, null)
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundColor(
                Color.TRANSPARENT
            )
        val tvTakePictures: TextView = dialogView.findViewById(R.id.tv_take_pictures)
        val tvOpenAlbum: TextView = dialogView.findViewById(R.id.tv_open_album)
        val tvCancel: TextView = dialogView.findViewById(R.id.tv_cancel)
        // 弹出弹窗的同时绑定点击事件，拍照
        tvTakePictures.setOnClickListener {
            takePhoto()
            bottomSheetDialog.cancel() // 点击后关闭弹窗
        }
        // 打开相册
        tvOpenAlbum.setOnClickListener {
            // 判断是否有读写权限
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Android6（API23）开始，读写SD卡在AndroidManifest.xml声明之后，还需动态申请权限
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            } else openAlbum()
            bottomSheetDialog.cancel()
        }
        // 取消
        tvCancel.setOnClickListener { bottomSheetDialog.cancel() }
        // 弹出弹窗
        bottomSheetDialog.show()
    }

    /**
     * 拍照
     * */
    @SuppressLint("SimpleDateFormat")
    private fun takePhoto() {
        // 创建File对象，用于存储拍照后的图片
        val timeStampFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
        val filename = timeStampFormat.format(Date())
        avatarFile = File(requireContext().externalCacheDir, filename + "_avatar.jpg")
        if (avatarFile.exists()) // 如果该文件存在，则删除后再创建新文件
            avatarFile.delete()
        avatarFile.createNewFile()
        // Android7.0以后读写文件需要配置Provider
        avatarUri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                FileProvider.getUriForFile(
                    requireContext(),
                    "com.finalprogram.yuemiao.fileprovider",
                    avatarFile
                )
            else Uri.fromFile(avatarFile)
        // 启动相机程序
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, avatarUri)
        avatar = avatarFile.absolutePath
        startActivityForResult(intent, takePhoto)
    }

    /**
     * 打开相册
     * */
    private fun openAlbum() {
        // 打开文件选择器
        val intent = Intent("android.intent.action.GET_CONTENT")
        // 指定只显示照片
        intent.type = "image/*"
        startActivityForResult(intent, fromAlbum)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            takePhoto -> {
                if (resultCode == Activity.RESULT_OK) {
                    // 将拍摄的照片显示出来
                    displayImage(avatar)
                }
            }
            fromAlbum -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    getAvatarPath(data) // 方法内部有调用到displayImage方法
                }
            }
        }
    }

    private fun getAvatarPath(data: Intent) {
        var imagePath: String? = null
        val uri = data.data
        Log.d("TAG", "handleImageOnKitKat: uri is $uri")
        // 如果是document类型的Uri，则通过document id处理
        if (DocumentsContract.isDocumentUri(requireContext(), uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri!!.authority) {
                val id = docId.split(":").toTypedArray()[1] // 解析出数字格式的id
                val selection = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(docId)
                )
                imagePath = getImagePath(contentUri, null)
            }
        }
        // 如果是content类型的Uri，则使用普通方式处理
        else if ("content".equals(uri!!.scheme, ignoreCase = true))
            imagePath = getImagePath(uri, null)
        // 如果是file类型的Uri，直接获取图片路径即可
        else if ("file".equals(uri.scheme, ignoreCase = true))
            imagePath = uri.path
        avatar = imagePath ?: null.toString()
        displayImage(imagePath)
    }

    @SuppressLint("Range")
    private fun getImagePath(uri: Uri, selection: String?): String? {
        var path: String? = null
        // 通过Uri和selection来获取真实的图片路径
        val cursor: Cursor? =
            requireContext().contentResolver.query(uri, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst())
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            cursor.close()
        }
        return path
    }

    /**
     * 动态申请权限时会回调到该方法
     * 该方法要在Fragment挂载的Activity中也重写，
     * */
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openAlbum() // 同意授权后，直接打开相册
            else Toast.makeText(
                MyApplication.context, "你拒绝了授权",
                Toast.LENGTH_SHORT
            ).show()
    }

    /**
     * 通过图片路径显示图片
     * */
    private fun displayImage(imagePath: String?) {
        if (!TextUtils.isEmpty(imagePath)) {
            //显示图片
            Glide.with(this).load(imagePath).apply(MyApplication.requestOptions)
                .into(binding.avatar)
            isSetAvatar = true
        } else
            Toast.makeText(
                MyApplication.context, "获取图片失败",
                Toast.LENGTH_SHORT
            ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}