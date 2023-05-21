package com.finalprogram.yuemiao.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity // 声明为Room实体类
data class User(
    var userAccount: String, // 账号
    var userPassword: String, // 密码
    var userName: String, // 用户名
    var isChecked: Boolean, // 是否记住密码
    var isLogged: Boolean, // 是否登录过
    var avatar: String, // 头像url
    var isLogging:Boolean // 是否正在登录
) : Serializable //序列化之后可以利用Intent传递对象数据
{
    /*设置主键*/
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
