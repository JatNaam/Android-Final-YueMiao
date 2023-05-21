package com.finalprogram.yuemiao.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.finalprogram.yuemiao.MainActivity
import com.finalprogram.yuemiao.database.entity.User
import com.finalprogram.yuemiao.databinding.FragmentHomeBinding
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.RectangleIndicator


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private var user: User? = null

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    /**
     * 获取activity的值
     */
    override fun onAttach(context: Context) {
        Log.d("HomeFragment", "onAttach")
        super.onAttach(context)
        (context as MainActivity).getUser().let {
            this.user = it
            Log.d("HomeFragment onAttach", it.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        Log.d("HomeFragment", "onCreateView")

        // 轮播图
        binding.banner.setAdapter(object : BannerImageAdapter<DataBean>(DataBean.bannerImages) {
            override fun onBindView(
                holder: BannerImageHolder,
                data: DataBean,
                position: Int,
                size: Int
            ) {
                Glide.with(holder.itemView)
                    .load(data.imageRes)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                    .into(holder.imageView)
            }
        })?.addBannerLifecycleObserver(this)?.indicator = RectangleIndicator(context)

        return binding.root
    }
}