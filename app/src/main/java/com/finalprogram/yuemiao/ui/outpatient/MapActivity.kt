package com.finalprogram.yuemiao.ui.outpatient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.PoiDetailInfo
import com.baidu.mapapi.search.poi.*
import com.finalprogram.yuemiao.BaseActivity
import com.finalprogram.yuemiao.MainActivity
import com.finalprogram.yuemiao.R
import com.finalprogram.yuemiao.databinding.ActivityMapBinding
import com.google.gson.Gson


/*
   百度地图应用，包含定位信息和地图显示
   一般需要打开定位服务，选择高精度定位模式，有网络连接
   需要在清单文件里使用百度云服务（参见清单文件service标签）
   需要创建应用（模块）的Key，并写入清单文件（参见清单文件meta标签）
*/
class MapActivity : BaseActivity() {
    private var mLocationClient //定位客户端
            : LocationClient? = null
    private var baiduMap: BaiduMap? = null

    private var location: Location? = null

    private var address: String? = null

    private var hasSearched = false

    // POI检索实例
    private var mPoiSearch: PoiSearch? = null

    private var poiDetailList: MutableList<PoiDetailInfo> = mutableListOf()

    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 普通返回
        binding.backOutpatient.setOnClickListener { finish() }
        // 确认定位并返回上一活动
        binding.confirmBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("location", location)
            val gson = Gson()
            val poiListString: String = gson.toJson(poiDetailList)
            intent.putExtra("poi", poiListString)
            // 携带参数返回上一个活动
            setResult(RESULT_OK, intent)
            finish()
        }

        // 手动刷新定位
        binding.relocate.setOnClickListener {
            mLocationClient!!.stop()
            binding.tvAdd.text = "定位中"
            hasSearched = false
            requestLocation()
        }

        // 设置搜索栏背景颜色（需要动态设置，在布局文件设置会无效）
        binding.searchView.setBackgroundResource(R.drawable.searchview_shape)
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 当用户点击搜索执行操作
                /**
                 *  PoiCiySearchOption 设置检索属性
                 *  city 检索城市
                 *  keyword 检索内容关键字
                 *  pageNum 分页页码
                 */
                mPoiSearch!!.searchInCity(
                    PoiCitySearchOption().city(query) //必填
                        .keyword("健康服务中心") //必填
                        .pageNum(0).pageCapacity(10)
                )
                // 收起软键盘
                hideKeyboard(binding.searchView)
                hasSearched = true
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 当用户输入文本时执行操作
                return true
            }
        })

        val layoutManager = LinearLayoutManager(this)
        binding.poiRecycleList.layoutManager = layoutManager

        // 创建POI检索实例
        mPoiSearch = PoiSearch.newInstance()
        // 设置POI检索的回调
        mPoiSearch!!.setOnGetPoiSearchResultListener(object : OnGetPoiSearchResultListener {
            // POI检索结果的回调
            override fun onGetPoiResult(poiResult: PoiResult) {
                binding.poiRecycleList.adapter = PoiAdapter(poiResult.allPoi)
                // 已经自动定位过了
                if (hasSearched) {
                    val location1 = poiResult.allPoi[0].location
                    location = Location(
                        poiResult.allPoi[0].city,
                        poiResult.allPoi[0].area,
                        location1.longitude,
                        location1.latitude
                    )
                    address =
                        poiResult.allPoi[0].province + poiResult.allPoi[0].city + poiResult.allPoi[0].area
                    mLocationClient!!.stop()
                    requestLocation()
                }

                // 检索poi结果的详细信息
                val uids = StringBuffer()
                poiResult.allPoi.forEach { run { uids.append(it.uid).append(",") } }
                mPoiSearch!!.searchPoiDetail(
                    PoiDetailSearchOption().poiUids(uids.toString())
                ) // uid的集合，最多可以传入10个uid，多个uid之间用英文逗号分隔。
            }

            // POI详细信息的回调
            override fun onGetPoiDetailResult(poiDetailSearchResult: PoiDetailSearchResult) {
                poiDetailList.clear()
                poiDetailList.addAll(poiDetailSearchResult.poiDetailInfoList)
            }

            override fun onGetPoiIndoorResult(poiIndoorResult: PoiIndoorResult) {}

            //废弃
            @Deprecated("Deprecated in Java")
            override fun onGetPoiDetailResult(poiDetailResult: PoiDetailResult) {
            }
        })

        try {
            requestLocation()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }


    @Throws(Exception::class)
    private fun requestLocation() {
        // 初始化地图组件
        baiduMap = binding.bmapView.map
        // 开启定位图层
        baiduMap!!.isMyLocationEnabled = true
        // 初始化定位服务
        mLocationClient = LocationClient(this)
        // 定位参数的配置
        val option = LocationClientOption()
        option.openGps = true
        option.setScanSpan(0) //设置扫描时间间隔，设置0表示只定位一次
        //设置定位模式，三选一
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
//        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
//        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true) //设置需要地址信息
        option.setIsNeedLocationDescribe(true) // 位置语义化结果
        option.setIsNeedLocationPoiList(true) // 周围POI结果

        // 注册LocationListener监听器
        mLocationClient!!.registerLocationListener(MyLocationListener())
        //保存定位参数
        mLocationClient!!.locOption = option
        // 启动定位服务
        mLocationClient!!.start()
    }

    //内部类，百度位置监听器
    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(bdLocation: BDLocation) {
            // 自动定位
            if (!hasSearched) {
                // 渲染当前详情地址到UI
                binding.tvAdd.text = bdLocation.addrStr
                // 保留当前位置城市、区县和经纬度
                location = Location(
                    bdLocation.city, bdLocation.district, bdLocation.longitude, bdLocation.latitude
                )
                /**
                 *  PoiCiySearchOption 设置检索属性
                 *  city 检索城市
                 *  keyword 检索内容关键字
                 *  pageNum 分页页码
                 */
                mPoiSearch!!.searchInCity(
                    PoiCitySearchOption().city(bdLocation.city) //必填
                        .keyword("健康服务中心") //必填
                        .pageNum(0).pageCapacity(10)
                )
            } else {
                binding.tvAdd.text = address
            }

            // 显示自身位置的蓝点箭头
            val locData = MyLocationData.Builder().accuracy(bdLocation.radius)// 设置定位数据的精度信息，单位：米
                .direction(bdLocation.direction) // 此处设置开发者获取到的方向信息，顺时针0-360
                .latitude(location!!.latitude).longitude(location!!.longitude).build()
            // 设置定位数据, 只有先允许定位图层后设置数据才会生效
            baiduMap!!.setMyLocationData(locData)

            if (bdLocation.locType == BDLocation.TypeGpsLocation || bdLocation.locType == BDLocation.TypeNetWorkLocation) {
                val latLng = LatLng(location!!.latitude, location!!.longitude)
                val builder = MapStatus.Builder()
                builder.target(latLng).zoom(20.0f)
                baiduMap!!.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()))
            }
        }
    }

    /**
     * 收起软键盘
     */
    fun hideKeyboard(view: View) {
        val inputMethodManager =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    // 管理MapView的生命周期
    override fun onResume() {
        super.onResume()
        binding.bmapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.bmapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationClient!!.stop()
        baiduMap!!.isMyLocationEnabled = false
        binding.bmapView.onDestroy()
        mPoiSearch!!.destroy()
    }
}