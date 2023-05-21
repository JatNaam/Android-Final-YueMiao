package com.finalprogram.yuemiao.ui.outpatient

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.finalprogram.yuemiao.MainActivity
import com.finalprogram.yuemiao.R
import com.finalprogram.yuemiao.database.entity.User
import com.finalprogram.yuemiao.databinding.FragmentOutpatientBinding
import com.finalprogram.yuemiao.ui.my.Menu
import java.util.*


class OutpatientFragment : Fragment() {
    private var _binding: FragmentOutpatientBinding? = null

    private val binding get() = _binding!!

    private var user: User? = null

    // 不可变列表List
    private val outpatients: List<Outpatient> = listOf(
        Outpatient(
            "深圳市南山区医疗集团总部龙瑞佳园社区健康服务中心",
            "门诊地址:兴海大道1048号龙瑞佳园山海居2栋2层16~19号房",
            "联系电话:0755-26918795",
            "距离：6.9km"
        ),
        Outpatient(
            "南山区医疗集团总部麻砌社区健康服务中心",
            "门诊地址: 深圳市南山区西丽麻工业北区商住楼一楼",
            "联系电话: 0755-86113133",
            "距离：7.7km"
        ),
        Outpatient(
            "南山医疗集团总部福光社区健康服务中心",
            "门诊地址:南山区留仙路1998号崇文花园5A临街层1-2层",
            "联系电话: 26916892",
            "距离：9.9km"
        ),
        Outpatient(
            "蛇口人民医院犬伤门诊",
            "门诊地址:深圳市南山区招商街道南山区蛇口工业七路36号",
            "联系电话: 0755-21606999",
            "距离：5.9km"
        ),
    )

    private val outpatientList: MutableList<Outpatient> = mutableListOf()

    private fun initOutpatientList() {
        val random = Random()
        for (i in 0..10) {
            val index = random.nextInt(outpatients.size)
            outpatientList.add(i, outpatients[index])
        }
    }

    companion object {
        fun newInstance(): OutpatientFragment {
            return OutpatientFragment()
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOutpatientBinding.inflate(inflater, container, false)
        // 设置搜索栏背景颜色（需要动态设置，在布局文件设置会无效）
        binding.searchView.setBackgroundResource(R.drawable.searchview_shape)
        initOutpatientList()
        val layoutManager = LinearLayoutManager(activity)
        binding.outpatientRecycleList.layoutManager = layoutManager
        binding.outpatientRecycleList.adapter = OutpatientAdapter(outpatientList)

        return binding.root
    }

}