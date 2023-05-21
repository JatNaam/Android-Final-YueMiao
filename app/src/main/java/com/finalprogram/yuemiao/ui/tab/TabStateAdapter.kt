package com.finalprogram.yuemiao.ui.tab

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 使用Lifecycle来管理viewpager中的Fragment生命周期，
 * 刚开始没找到正确的从activity传值到子fragment的方法，所以采用了监听viewpager2的滑动事件再配合bundle传值，
 * 这个方法是当fragment对用户可见时即触发onResume生命周期，再到onResume生命周期中用bundle取值。
 *
 * 后来找到了fragment在onAttack生命周期方法获取符activity值的方法，所以这里的lifecycle没用了
 */
class TabStateAdapter(
    fragmentManager: FragmentManager,
    private val fragmentList: List<Fragment>,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
//        when (position) {
//            0 -> return HomeFragment.newInstance()
//            1 -> return OutpatientFragment.newInstance()
//        }
//        return MyFragment.newInstance()
        return fragmentList[position]
    }

}