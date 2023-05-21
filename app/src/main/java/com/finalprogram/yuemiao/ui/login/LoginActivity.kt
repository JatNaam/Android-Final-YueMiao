package com.finalprogram.yuemiao.ui.login

import android.os.Bundle
import com.finalprogram.yuemiao.BaseActivity
import com.finalprogram.yuemiao.databinding.ActivityLoginBinding

class LoginActivity : BaseActivity(){
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}