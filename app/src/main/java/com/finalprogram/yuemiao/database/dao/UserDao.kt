package com.finalprogram.yuemiao.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.finalprogram.yuemiao.database.entity.User

/**
 * 除@Query注解要编写SQL语句外，其他操作均只要添加注解即可
 */
@Dao // 使用该注解，Room数据库才能把该接口识别成一个Dao
interface UserDao {
    @Insert
    fun insertUser(user: User): Long

    @Update
    fun updateUser(user: User)

    @Query("select * from User")
    fun loadAllUsers(): List<User>

    @Query("select * from User where isLogged = :flag")
    fun loadLoggedUsers(flag: Boolean): List<User>

    @Query("select * from User where userAccount = :userAccount")
    fun loadUser(userAccount: String): User?

    @Query("select * from User where isLogging=:flag")
    fun loadLoggingUser(flag: Boolean): User?

    @Query("delete from User where userAccount = :userAccount")
    fun deleteUserByAccount(userAccount: String): Int
}