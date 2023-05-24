package com.finalprogram.yuemiao.ui.my

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.finalprogram.yuemiao.MainActivity
import com.finalprogram.yuemiao.MyApplication
import com.finalprogram.yuemiao.R
import com.finalprogram.yuemiao.database.entity.User
import com.finalprogram.yuemiao.databinding.FragmentMyBinding
import com.finalprogram.yuemiao.ui.login.LoginActivity


class MyFragment : Fragment() {
    private var _binding: FragmentMyBinding? = null

    private val binding get() = _binding!!

    private var user: User? = null

    // 不可变列表List
    private val menuList: List<Menu> = listOf(
        Menu(R.drawable.person_information, "受种者信息"),
        Menu(R.drawable.certification_information, "认证信息"),
        Menu(R.drawable.privacy_policy, "隐私政策"),
        Menu(R.drawable.operation_manual, "操作手册"),
        Menu(R.drawable.common_problem, "常见问题"),
        Menu(R.drawable.check_update, "检查更新")
    )

    companion object {
        fun newInstance(): MyFragment {
            return MyFragment()
        }
    }

    /**
     * 获取activity的值
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as MainActivity).getUser().let {
            this.user = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(activity)
        binding.menuRecycleList.layoutManager = layoutManager
        binding.menuRecycleList.adapter = MenuAdapter(menuList)

        binding.loginBtn.setOnClickListener { login() }
        // 取出登录活动发送到主活动的数据
        if (user != null)
            loadUI()

        return binding.root
    }

    /**
     * 懒加载（只对网络请求有用）要在这里实现（搭配懒定义的变量使用）
     */
    override fun onResume() {
        super.onResume()
    }

    /**
     * 跳转至登录活动
     */
    private fun login() {
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
    }

    /**
     * 退出登录
     */
    private fun logout() {
        val intent = Intent("com.finalprogram.yuemiao.Force_Offline")
        activity?.sendBroadcast(intent) // 发送强制下线广播
    }

    /**
     * 有正在登录的账号，渲染UI并进行点击事件的更新
     */
    private fun loadUI() {
        binding.userAvatar.let {
            Glide.with(this).load(user!!.avatar).apply(MyApplication.requestOptions)
                .into(it)
        }
        binding.userName.text = user!!.userName
        // 使退出按钮显示并绑定注销事件
        binding.logoutBtn.visibility = View.VISIBLE
        binding.logoutBtn.isClickable = true
        binding.logoutBtn.setOnClickListener {
            logout()
        }
        binding.loginBtn.setOnClickListener(null)
    }
}