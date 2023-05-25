package com.finalprogram.yuemiao.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.finalprogram.yuemiao.*
import com.finalprogram.yuemiao.database.AppDatabase
import com.finalprogram.yuemiao.database.dao.UserDao
import com.finalprogram.yuemiao.database.entity.User
import com.finalprogram.yuemiao.databinding.ActivityLoginBinding
import com.finalprogram.yuemiao.network.NetWorkUtil
import com.finalprogram.yuemiao.ui.register.RegisterActivity
import kotlin.concurrent.thread

class LoginActivity : BaseActivity(){
    private lateinit var binding: ActivityLoginBinding

    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sp: SharedPreferences =
            MyApplication.context.getSharedPreferences("LoggedAccountBefore", 0)

        userDao = AppDatabase.getDatabase(MyApplication.context).getUserDao()

        binding.loginAccountEdit.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isNotEmpty()) {
                // 开启子线程查询数据库
                thread {
                    val user: User? = userDao.loadUser(content)
                    if (user != null) {
                        runOnUiThread {
                            Glide.with(this).load(user.avatar)
                                .apply(MyApplication.requestOptions)
                                .into(binding.userAvatar)
                            // 如果用户记住密码则勾上CheckBox并自动填充密码，否则。。。
                            binding.rememberCheck.isChecked = user.isChecked
                            if (user.isChecked)
                                binding.loginPasswordEdit.setText(user.userPassword)
                            else binding.loginPasswordEdit.text = null
                        }
                    }
                }
            }
        }

        /**
         * 判断是在什么情况打开登录界面的
         * */
        var intent = intent
        if (intent != null) {
            when (intent.getStringExtra("STATUS")) {
                null -> {
                    // 重新登录，填上上一个登录的账号
                    val loggedAccountBefore: String? = sp.getString("loggedAccountBefore", null)
                    binding.loginAccountEdit.setText(loggedAccountBefore)
                }
                "REGISTER" -> {
                    // 填上刚刚注册完成的账号
                    val registerAccount = intent.getStringExtra("registerAccount")
                    binding.loginAccountEdit.setText(registerAccount)
                }
            }
        }

        binding.loginBtn.setOnClickListener {
            val account: String = binding.loginAccountEdit.text.toString()
            val password: String = binding.loginPasswordEdit.text.toString()
            if (NetWorkUtil.isNetworkConnected(MyApplication.context)) {
                if (account.length in 6..10 && password.length in 8..15) {
                    thread {
                        val user: User? = userDao.loadUser(account)
                        if (user != null) {
                            if (user.userPassword == password) {
                                user.isChecked = binding.rememberCheck.isChecked
                                user.isLogged = true
                                user.isLogging = true
                                userDao.updateUser(user)
                                /*利用intent传递序列化之后的对象数据*/
                                intent = Intent(this, MainActivity::class.java).apply {
                                    putExtra("userData", user)
                                }
                                // 保存登录的账号，用于重新打开程序时填写信息
                                // 重新更新users的数据
                                @SuppressLint("CommitPrefEdits") val editor = sp.edit()
                                editor.putString("loggedAccountBefore", account)
                                editor.apply()

                                startActivity(intent)
                                ActivityCollector.finishAll() //登录跳转，销毁登录之前的所有活动
                            } else {
                                runOnUiThread {
                                    Toast.makeText(
                                        MyApplication.context, "密码错误",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.loginPasswordEdit.text = null
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    MyApplication.context, "该账号不存在",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.loginAccountEdit.text = null
                                binding.loginPasswordEdit.text = null
                            }
                        }
                    }
                } else Toast.makeText(
                    MyApplication.context, "请检查你的输入信息是否正确",
                    Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(
                MyApplication.context, "网络异常",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.registerBtn.setOnClickListener {
            intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.showListBtn.setOnClickListener { showPopupWindow() }

    }


    private fun showPopupWindow() {
        //设置contentView
        @SuppressLint("InflateParams") val contentView: View =
            layoutInflater.inflate(R.layout.popupwindow_accounts, null)
        val mPopWindow = PopupWindow(
            contentView,
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true
        )
        mPopWindow.contentView = contentView
        // 实现PopupWindow+ListView切换账户
        val listView = contentView.findViewById<View>(R.id.accountList) as ListView
        // 数据库操作必须在子线程
        thread {
            // 查询登录过的用户
            val loggedUsers: List<User> = userDao.loadLoggedUsers(true)
            if (loggedUsers.isNotEmpty()) {
                val adapter = AccountAdapter(R.layout.item_account, loggedUsers)
                listView.adapter = adapter
                listView.onItemClickListener =
                    AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                        runOnUiThread {
                            binding.loginAccountEdit.setText(loggedUsers[position].userAccount)
                            mPopWindow.dismiss() // 选择后关闭下拉列表
                        }
                    }
            }
        }
        // 在下拉按钮下方显示PopupWindow
        mPopWindow.showAsDropDown(binding.loginAccountEdit)
    }

    inner class AccountAdapter(
        private val resourceId: Int,
        objects: List<User?>?
    ) :
        ArrayAdapter<User?>(MyApplication.context, resourceId, objects!!) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val accountItem = getItem(position) // 获取当前项的实例
            val view: View
            val viewHolder: ViewHolder
            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(resourceId, parent, false)
                viewHolder = ViewHolder()
                viewHolder.account = view.findViewById<View>(R.id.account) as TextView
                view.tag = viewHolder // 将ViewHolder存储在View中
            } else {
                view = convertView
                viewHolder = view.tag as ViewHolder // 重新获取ViewHolder
            }
            if (accountItem != null)
                viewHolder.account.text = accountItem.userAccount
            return view
        }

        inner class ViewHolder {
            lateinit var account: TextView
        }
    }

}