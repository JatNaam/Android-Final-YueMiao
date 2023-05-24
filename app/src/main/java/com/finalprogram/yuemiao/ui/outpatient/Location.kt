package com.finalprogram.yuemiao.ui.outpatient

import java.io.Serializable

data class Location(
    var city: String,
    var district: String,
    var longitude: Double,// 经度
    var latitude: Double// 纬度
) : Serializable

