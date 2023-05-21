package com.finalprogram.yuemiao.ui.home

import com.finalprogram.yuemiao.R

/**
 * 轮播图图片类
 */
class DataBean {
    var imageRes: Int? = null
    var imageUrl: String? = null
    var title: String?
    var viewType: Int

    constructor(imageRes: Int?, title: String?, viewType: Int) {
        this.imageRes = imageRes
        this.title = title
        this.viewType = viewType
    }

    constructor(imageUrl: String?, title: String?, viewType: Int) {
        this.imageUrl = imageUrl
        this.title = title
        this.viewType = viewType
    }

    companion object {
        val bannerImages: List<DataBean>
            get() {
                val list: MutableList<DataBean> = ArrayList()
                list.add(
                    DataBean(
                        R.drawable.banner1,
                        null,
                        1
                    )
                )
                list.add(
                    DataBean(
                        R.drawable.banner2,
                        null,
                        1
                    )
                )
                list.add(
                    DataBean(
                        R.drawable.banner3,
                        null,
                        1
                    )
                )
                return list
            }
    }
}