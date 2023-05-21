package com.finalprogram.yuemiao.ui.my;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finalprogram.yuemiao.MyApplication;
import com.finalprogram.yuemiao.R;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private final List<Menu> menuList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View menuView;
        ImageView imageView;
        TextView textView;

        public ViewHolder(View view) {
            super(view);
            menuView = view;
            imageView = view.findViewById(R.id.img_menu);
            textView = view.findViewById(R.id.text_menu);
        }
    }

    public MenuAdapter(List<Menu> list) {
        menuList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 用于创建一个ViewHolder实例，将news_item布局加载进来。
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);

        final ViewHolder holder = new ViewHolder(view);
        holder.menuView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Toast.makeText(
                    MyApplication.context, "你点击了" + menuList.get(position).getText() + ",功能尚未开发！",
                    Toast.LENGTH_SHORT
            ).show();
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //用于对RecyclerView子项的数据进行赋值，会在每个子项滚动到屏幕内的时候执行，
        //这里我们通过position参数得到当前项的实例，然后再将数据设置到ViewHolder的控件中
        String text = menuList.get(position).getText();
        int icon = menuList.get(position).getIcon();
        holder.textView.setText(text);
        holder.imageView.setImageResource(icon);
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }
}
