package com.finalprogram.yuemiao.ui.outpatient;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.finalprogram.yuemiao.MyApplication;
import com.finalprogram.yuemiao.R;

import java.util.List;

public class OutpatientAdapter extends RecyclerView.Adapter<OutpatientAdapter.ViewHolder> {
    private final List<PoiDetailInfo> outpatientList;

    private final Location location;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View outpatientView;
        TextView nameText;
        TextView addressText;
        TextView phoneText;
        TextView distanceText;

        public ViewHolder(View view) {
            super(view);
            outpatientView = view;
            nameText = view.findViewById(R.id.name);
            addressText = view.findViewById(R.id.address);
            phoneText = view.findViewById(R.id.phone);
            distanceText = view.findViewById(R.id.distance);
        }
    }

    public OutpatientAdapter(List<PoiDetailInfo> list, Location location) {
        outpatientList = list;
        this.location = location;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 用于创建一个ViewHolder实例，将news_item布局加载进来。
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outpatient, parent, false);

        final ViewHolder holder = new ViewHolder(view);
        holder.outpatientView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Toast.makeText(
                    MyApplication.context, "你点击了" + outpatientList.get(position).getName() + ",功能尚未开发！",
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
        String name = outpatientList.get(position).getName();
        String address = outpatientList.get(position).getAddress();
        String phone = outpatientList.get(position).getTelephone();
        double longitude = outpatientList.get(position).getLocation().longitude;
        double latitude = outpatientList.get(position).getLocation().latitude;
        @SuppressLint("DefaultLocale") String distance = String.format("距离：%.2fkm", getDistance(location.getLatitude(), location.getLongitude(), latitude, longitude));
        holder.nameText.setText(name);
        holder.addressText.setText(address);
        holder.phoneText.setText(phone);
        holder.distanceText.setText(distance);
    }

    @Override
    public int getItemCount() {
        return outpatientList.size();
    }

    /**
     * 通过两点经纬度计算距离
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c;
    }

}
