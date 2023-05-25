package com.finalprogram.yuemiao.ui.outpatient

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.mapapi.search.core.PoiDetailInfo
import com.finalprogram.yuemiao.MainActivity
import com.finalprogram.yuemiao.MyApplication
import com.finalprogram.yuemiao.R
import com.finalprogram.yuemiao.database.entity.User
import com.finalprogram.yuemiao.databinding.FragmentOutpatientBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.permissionx.guolindev.PermissionX


class OutpatientFragment : Fragment() {
    private var _binding: FragmentOutpatientBinding? = null

    private val binding get() = _binding!!

    private var user: User? = null

    private var location: Location? = null

    private var poiList: MutableList<PoiDetailInfo> = mutableListOf()

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
        Log.d("OutpatientFragment", "onAttach")
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

        Log.d("OutpatientFragment", "onCreate")

        // 从定位活动返回触发的回调方法，替代了在API29遗弃的startActivityForResult和onActivityResult的方法
        // 返回不是通过活动跳转来实现，而是通过把栈顶活动从活动栈出栈更新栈顶活动来实现，
        // 讲解一下为什么不用活动跳转实现返回操作：
        /*
        * 如果通过活动跳转来实现返回操作的话，默认跳转则会创建的新的活动实例来入栈，此时从MapActivity返回的
        * MainActivity不是我们的目的活动，我们的目标活动还在栈底；若要跳转会我们的目标MainActivity的话，可
        * 以将Intent的跳转模式设置为SingleTask模式，虽然该方法实现了我们的目的，但是该方法会使MainActivity
        * 重新创建，使UI和数据都重新渲染，这样会把活动上嵌套的Fragment销毁并重建，无法返回到MainActivity上的
        * viewpager对应的页面。
        * 因此我们不能使用活动跳转来实现返回操作
        * */
        val activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                //获取返回的结果
                location = it.data!!.getSerializableExtra("location") as Location?
                Log.d("MainActivity-OutpatientFragment", location.toString())
                // 渲染返回的位置信息到UI
                binding.city.text = location!!.city
                binding.region.text = location!!.district
                binding.latitudeLongitude.text =
                    String.format("经纬度(%.2f,%.2f)", location!!.longitude, location!!.latitude)
                // 解析返回的POI列表
                val gson = Gson()
                val poiStr = it.data!!.getStringExtra("poi") as String
                Log.d("MainActivity-OutpatientFragment", poiStr)
                val type = object : TypeToken<MutableList<PoiDetailInfo>>() {}.type
                poiList.addAll(gson.fromJson(poiStr, type))
                binding.outpatientRecycleList.adapter = OutpatientAdapter(poiList, location)
            }
        }

        // 设置搜索栏背景颜色（需要动态设置，在布局文件设置会无效）
        binding.searchView.setBackgroundResource(R.drawable.searchview_shape)

        val layoutManager = LinearLayoutManager(activity)
        binding.outpatientRecycleList.layoutManager = layoutManager
        binding.citySelect.setOnClickListener {
            // 通过PermissionX插件动态申请定位需要的权限
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE
                )
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        Log.d("Location Grant：", grantedList.toString())
                        val intent = Intent(context, MapActivity::class.java)
                        // 新的跳转方法
                        activityResultLauncher.launch(intent)
                    } else {
                        Toast.makeText(
                            MyApplication.context,
                            " You denied $deniedList",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        return binding.root
    }

    /**
     * 获取焦点，即对应的viewpager页面对用户可见
     */
    override fun onResume() {
        super.onResume()
        Log.d("OutpatientFragment", "onResume")

    }

    /**
     * 失去焦点
     */
    override fun onPause() {
        super.onPause()
        Log.d("OutpatientFragment", "onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}