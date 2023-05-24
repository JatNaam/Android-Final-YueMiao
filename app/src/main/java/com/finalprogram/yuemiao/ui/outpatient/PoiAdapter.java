package com.finalprogram.yuemiao.ui.outpatient;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.finalprogram.yuemiao.MyApplication;
import com.finalprogram.yuemiao.R;

import java.util.List;

public class PoiAdapter extends RecyclerView.Adapter<PoiAdapter.ViewHolder> {
    private final List<PoiInfo> poiList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View poiView;
        TextView nameText;
        TextView addressText;

        public ViewHolder(View view) {
            super(view);
            poiView = view;
            nameText = view.findViewById(R.id.text_name);
            addressText = view.findViewById(R.id.text_addr);
        }
    }

    public PoiAdapter(List<PoiInfo> list) {
        poiList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 用于创建一个ViewHolder实例，将news_item布局加载进来。
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_poi, parent, false);

        final ViewHolder holder = new ViewHolder(view);
        holder.poiView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Toast.makeText(
                    MyApplication.context, "你点击了" + poiList.get(position).getName() + ",功能尚未开发！",
                    Toast.LENGTH_SHORT
            ).show();
        });
        return holder;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //用于对RecyclerView子项的数据进行赋值，会在每个子项滚动到屏幕内的时候执行，
        //这里我们通过position参数得到当前项的实例，然后再将数据设置到ViewHolder的控件中
        String name = poiList.get(position).getName();
        String address = poiList.get(position).getAddress();
        holder.nameText.setText(name);
        holder.addressText.setText(address);
    }

    @Override
    public int getItemCount() {
        return poiList.size();
    }
}

