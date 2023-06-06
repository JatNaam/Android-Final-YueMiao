package com.finalprogram.yuemiao.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.finalprogram.yuemiao.MainActivity
import com.finalprogram.yuemiao.MyApplication
import com.finalprogram.yuemiao.database.entity.User
import com.finalprogram.yuemiao.databinding.FragmentHomeBinding
import com.finalprogram.yuemiao.databinding.HomeBookTopicBinding
import com.finalprogram.yuemiao.databinding.HomeCovTopicBinding
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.RectangleIndicator


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var bookTopicBinding: HomeBookTopicBinding

    private lateinit var covTopicBinding: HomeCovTopicBinding

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
        bookTopicBinding = HomeBookTopicBinding.bind(binding.root)
        covTopicBinding = HomeCovTopicBinding.bind(binding.root)
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

        bookTopicBinding.inoculationRefer.setOnClickListener {
            val intent = Intent(activity, ReferActivity::class.java)
            startActivity(intent)
        }

        bookTopicBinding.kidInoculation.setOnClickListener(clickHandler("儿童接种"))
        bookTopicBinding.adultInoculation.setOnClickListener(clickHandler("成人接种"))
        bookTopicBinding.hpvInoculation.setOnClickListener(clickHandler("HPV接种"))

        bookTopicBinding.bookRecord.setOnClickListener(clickHandler("预约记录"))
        bookTopicBinding.inoculationRecord.setOnClickListener(clickHandler("接种记录"))
        bookTopicBinding.inoculationProve.setOnClickListener(clickHandler("接种凭证"))
        bookTopicBinding.comQuest.setOnClickListener(clickHandler("常见问题"))

        covTopicBinding.covRow1.setOnClickListener(clickHandler("个人新冠疫苗接种预约"))
        covTopicBinding.covRow2.setOnClickListener(clickHandler("团体新冠疫苗接种预约"))
        covTopicBinding.covRow3.setOnClickListener(clickHandler("新冠疫苗报名登记"))
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun clickHandler(text: String): View.OnClickListener {
        return View.OnClickListener {
            Toast.makeText(
                MyApplication.context, "你点击了$text,功能尚未开发！",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}