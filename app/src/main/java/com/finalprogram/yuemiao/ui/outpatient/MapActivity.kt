package com.finalprogram.yuemiao.ui.outpatient

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.finalprogram.yuemiao.MyApplication
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

    private var isFirstLocate = true

    private var location: Location? = null

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
                    PoiCitySearchOption()
                        .city(query) //必填
                        .keyword("健康服务中心") //必填
                        .pageNum(0)
                        .pageCapacity(10)
                )
                // 收起软键盘
                hideKeyboard(binding.searchView)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 当用户输入文本时执行操作
                return true
            }
        })

        val layoutManager = LinearLayoutManager(this)
        binding.poiRecycleList.layoutManager = layoutManager

        mPoiSearch = PoiSearch.newInstance()
        // POI检索的回调方法
        val listener: OnGetPoiSearchResultListener = object : OnGetPoiSearchResultListener {
            override fun onGetPoiResult(poiResult: PoiResult) {
                binding.poiRecycleList.adapter = PoiAdapter(poiResult.allPoi)
                // 检索poi结果的详细信息
                val uids = StringBuffer()
                poiResult.allPoi.forEach { run { uids.append(it.uid).append(",") } }
                mPoiSearch!!.searchPoiDetail(
                    PoiDetailSearchOption()
                        .poiUids(uids.toString())
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
        }
        mPoiSearch!!.setOnGetPoiSearchResultListener(listener)

        try {
            requestLocation()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }


    @Throws(Exception::class)
    private fun requestLocation() {
        //初始化
        baiduMap = binding.bmapView.map
        mLocationClient = LocationClient(this)
        // 开启定位图层
        baiduMap!!.isMyLocationEnabled = true

        val option = LocationClientOption()
        option.openGps = true
        //设置扫描时间间隔，设置0表示只定位一次
        option.setScanSpan(0)
        //设置定位模式，三选一
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        /*option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);*/
        //设置需要地址信息
        option.setIsNeedAddress(true)
        // 位置语义化结果
        option.setIsNeedLocationDescribe(true)
        // POI结果
        option.setIsNeedLocationPoiList(true)

        // 注册LocationListener监听器
        mLocationClient!!.registerLocationListener(MyLocationListener())
        //保存定位参数
        mLocationClient!!.locOption = option
        mLocationClient!!.start()
    }

    //内部类，百度位置监听器
    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(bdLocation: BDLocation) {

            binding.tvAdd.text = bdLocation.addrStr

            location = Location(
                bdLocation.city,
                bdLocation.district,
                bdLocation.longitude,
                bdLocation.latitude
            )

            /**
             *  PoiCiySearchOption 设置检索属性
             *  city 检索城市
             *  keyword 检索内容关键字
             *  pageNum 分页页码
             */
            mPoiSearch!!.searchInCity(
                PoiCitySearchOption()
                    .city(bdLocation.city) //必填
                    .keyword("健康服务中心") //必填
                    .pageNum(0)
                    .pageCapacity(10)
            )

            val locData = MyLocationData.Builder()
                .accuracy(bdLocation.radius)// 设置定位数据的精度信息，单位：米
                .direction(bdLocation.direction) // 此处设置开发者获取到的方向信息，顺时针0-360
                .latitude(bdLocation.latitude)
                .longitude(bdLocation.longitude)
                .build()
            // 设置定位数据, 只有先允许定位图层后设置数据才会生效
            baiduMap!!.setMyLocationData(locData)

            if (bdLocation.locType == BDLocation.TypeGpsLocation || bdLocation.locType == BDLocation.TypeNetWorkLocation) {
                if (isFirstLocate) {
                    isFirstLocate = false
                    val latLng = LatLng(bdLocation.latitude, bdLocation.longitude)
                    val builder = MapStatus.Builder()
                    builder.target(latLng).zoom(20.0f)
                    baiduMap!!.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()))
                }
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
            view.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
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