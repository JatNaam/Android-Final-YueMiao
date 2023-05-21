package com.finalprogram.yuemiao.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.finalprogram.yuemiao.database.dao.UserDao
import com.finalprogram.yuemiao.database.entity.User

/*
定义Room数据库分三步：设置实体类，设置Dao，设置Database
*/

//定义Database，三个部分内容：版本号、包含哪些实体类、Dao层的访问实例
@Database(version = 1, entities = [User::class])
abstract class AppDatabase : RoomDatabase() {
    /**
     * 用于获取Dao实例，只需声明即可，具体方法Room在底层会自动实现
     * */
    abstract fun getUserDao(): UserDao
    /**
     * 单例模式，因为原则上全局只存在一份AppDatabase的实例
     * 用于缓存AppDatabase实例
     * */
    companion object {
        private var instance: AppDatabase? = null
        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let {
                return it
            }
            /**
             * 三个参数：
             * 第一个不能使用普通的context，必须是下面这个，不然容易内存溢出；
             * 第二个是AppDatabase的class类型
             * 第三个是数据库的名称
             * */
            return Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "app_database"
            ).build().apply {
                instance = this
            }
        }
    }

    /*JAVA的写法*/
//    private var instance: AppDatabase? = null
//    @Synchronized
//    open fun getDatabase(context: Context): AppDatabase? {
//        if (instance == null) {
//            /*三个参数：第一个不能使用普通的context，必须是下面这个，不让容易内存溢出；
//                      第二个是AppDatabase的class类型
//                      第三个是数据库的名称*/
//            instance = Room.databaseBuilder(
//                context.applicationContext, AppDatabase::class.java,"app_database"
//            ).build()
//        }
//        return instance
//    }
}